package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.structures.polyCommutativeRing
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import java.math.BigInteger

/**
 * Evaluation `p ↦ p(a)` (Horner) and substitution `p ↦ p ∘ q`.
 *
 * Over a commutative ring both are ring homomorphisms: evaluation at `a` is `R[x] → R`, and
 * substitution of `q` is `R[x] → R[x]` sending `x ↦ q`. Over a noncommutative ring evaluation is a
 * homomorphism only at central points; the kernel puts coefficients on the left, `Σ cᵢ·aⁱ`.
 */
class PolyEvalSubstSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val zKit = zz.polyKit(eqZ)
    val zModel = ListPolyModel(zz, eqZ)
    val polys = ArbPoly.poly(zKit, ArbPoly.smallInteger)
    val points: Arb<BigInteger> = Arb.int(-5..5).map(Int::toBigInteger)

    fun zPoly(vararg cs: Int) = ArbPoly.polyOf(zKit, ints(*cs))

    /* --------------------------
     * Evaluation
     * -------------------------- */

    test("ℤ[x]: Horner evaluation agrees with Σ cᵢ·aⁱ") {
        checkAll(polys, points) { p, a ->
            zKit.eval(p, a) shouldBe zModel.eval(p.coeffs, a)
        }
    }

    test("ℤ/4[x]: Horner evaluation agrees with Σ cᵢ·aⁱ") {
        val z4 = integersMod(4)
        val kit = z4.polyKit()
        val model = ListPolyModel(z4)
        checkAll(ArbPoly.poly(kit, ArbPoly.residue(4)), ArbPoly.residue(4)) { p, a ->
            kit.eval(p, a) shouldBe model.eval(p.coeffs, a)
        }
    }

    test("ℍ_ℚ[x]: Horner evaluation agrees with Σ cᵢ·aⁱ (coefficients on the left)") {
        val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val kit = hq.polyKit()
        val model = ListPolyModel(hq)
        val smrq = ArbRationalQuaternion.smallRationalQuaternion
        checkAll(ArbPoly.poly(kit, smrq, maxDegree = 3), smrq) { p, a ->
            kit.eval(p, a) shouldBe model.eval(p.coeffs, a)
        }
    }

    test("evaluating 0, a constant, x, and at 0") {
        zKit.run {
            checkAll(points, ArbPoly.smallInteger, polys) { a, c, p ->
                eval(zeroP, a) shouldBe BigInteger.ZERO
                eval(embed(c), a) shouldBe c
                eval(x(), a) shouldBe a
                eval(p, BigInteger.ZERO) shouldBe p.coeffs.firstOrNull().let { it ?: BigInteger.ZERO }
            }
        }
    }

    listOf(-2, 0, 1, 3).forEach { n ->
        test("ℤ[x]: evaluation at $n is a ring homomorphism ℤ[x] → ℤ") {
            val a = n.toBigInteger()
            RingHomomorphismLaws(
                hom = { p: Poly<BigInteger> -> zKit.eval(p, a) },
                domain = zz.polyCommutativeRing(eqZ),
                codomain = zz,
                arb = polys,
            ).fullTest().throwIfFailed()
        }
    }

    test("ℍ_ℚ[x]: evaluation is multiplicative at a central (rational) point, but not at i") {
        val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val kit = hq.polyKit()
        val qPolys = ArbPoly.poly(kit, ArbRationalQuaternion.smallRationalQuaternion, maxDegree = 2)
        val central = Arb.int(-3..3).map { n ->
            quaternion(Rational.of(n), Rational.ZERO, Rational.ZERO, Rational.ZERO)
        }

        kit.run {
            checkAll(qPolys, qPolys, central) { p, q, a ->
                eval(mulP(p, q), a) shouldBe hq.mul(eval(p, a), eval(q, a))
            }
        }

        // p = x, q = j (constant): pq = j·x, so (pq)(i) = j·i = -k, but p(i)·q(i) = i·j = k.
        val zero = Rational.ZERO
        val one = Rational.ONE
        val i = quaternion(zero, one, zero, zero)
        val j = quaternion(zero, zero, one, zero)
        val k = quaternion(zero, zero, zero, one)

        kit.run {
            val p = x()
            val q = embed(j)
            eval(mulP(p, q), i) shouldBe hq.add.inverse(k)
            hq.mul(eval(p, i), eval(q, i)) shouldBe k
        }
    }

    test("𝔽₅[x]: x⁵ - x is a nonzero polynomial that vanishes on all of 𝔽₅ (Fermat)") {
        val f5 = PrimeField(5.toBigInteger())
        val kit = f5.polyKit()
        kit.run {
            val p = addP(xPow(5), neg(x()))
            (p == zeroP) shouldBe false
            (0 until 5).forEach { a -> eval(p, a.toBigInteger()) shouldBe BigInteger.ZERO }
        }
    }

    /* --------------------------
     * Substitution
     * -------------------------- */

    val small = ArbPoly.poly(zKit, ArbPoly.smallInteger, maxDegree = 2)

    test("(x² + 1) ∘ (x + 1) = x² + 2x + 2") {
        zKit.subst(zPoly(1, 0, 1), zPoly(1, 1)).coeffs shouldBe ints(2, 2, 1)
    }

    test("substitution is compatible with evaluation: (p ∘ q)(a) = p(q(a))") {
        zKit.run {
            checkAll(polys, small, points) { p, q, a ->
                eval(subst(p, q), a) shouldBe eval(p, eval(q, a))
            }
        }
    }

    test("x is the identity for ∘ on both sides, and constants absorb on the left") {
        zKit.run {
            checkAll(polys, ArbPoly.smallInteger) { p, c ->
                subst(p, x()) shouldBe p
                subst(x(), p) shouldBe p
                subst(embed(c), p) shouldBe embed(c)
            }
        }
    }

    test("substituting a constant is evaluation: p ∘ c = p(c)") {
        zKit.run {
            checkAll(polys, ArbPoly.smallInteger) { p, c ->
                subst(p, embed(c)) shouldBe embed(eval(p, c))
            }
        }
    }

    test("composition is associative: (p ∘ q) ∘ r = p ∘ (q ∘ r)") {
        zKit.run {
            checkAll(small, small, small) { p, q, r ->
                subst(subst(p, q), r) shouldBe subst(p, subst(q, r))
            }
        }
    }

    test("substituting q is a ring homomorphism ℤ[x] → ℤ[x]") {
        zKit.run {
            checkAll(small, small, small) { p, r, q ->
                subst(addP(p, r), q) shouldBe addP(subst(p, q), subst(r, q))
                subst(mulP(p, r), q) shouldBe mulP(subst(p, q), subst(r, q))
            }
            checkAll(small) { q -> subst(oneP, q) shouldBe oneP }
        }
    }

    test("ℤ[x]: deg(p ∘ q) = deg p · deg q when p ≠ 0 and deg q ≥ 1") {
        checkAll(polys, small) { p, q ->
            if (p != zKit.zeroP && q.degree >= 1) {
                zKit.subst(p, q).degree shouldBe p.degree * q.degree
            }
        }
    }
})
