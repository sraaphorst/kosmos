package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion

/**
 * The arithmetic of `PolyKernel`, seen through [PolyKit]: normalization, `+`, `·` and negation
 * agree with the naive [ListPolyModel], results are always canonical, and degrees behave.
 *
 * Every coefficient ring here is checked the same way, so each one is a separate "lens" on the
 * same kernel:
 * - `ℕ` (a semiring: no negation),
 * - `ℤ` and `ℚ` (integral domains),
 * - `𝔽₂` and `𝔽₅` (positive characteristic),
 * - `ℤ/4` (zero divisors, so leading coefficients can cancel in a product),
 * - `ℍ_ℚ` (noncommutative, so the order of coefficient products matters).
 */
class PolyArithmeticSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val zKit = zz.polyKit(eqZ)

    /**
     * Register the model-based checks for one coefficient ring.
     */
    fun <A : Any> modelChecks(
        label: String,
        coeff: Semiring<A>,
        kit: PolyKit<A>,
        coeffArb: Arb<A>,
        negA: ((A) -> A)? = null,
        eqA: Eq<A> = Eq.default()
    ) {
        val model = ListPolyModel(coeff, eqA)
        val lists = ArbPoly.coeffLists(coeffArb)
        val polys = ArbPoly.poly(kit, coeffArb)

        fun isCanonical(p: Poly<A>): Boolean =
            p.coeffs.isEmpty() || !eqA(p.coeffs.last(), coeff.zero)

        test("$label: building from a raw coefficient list just drops trailing zeros") {
            checkAll(lists) { cs ->
                ArbPoly.polyOf(kit, cs).coeffs shouldBe model.trim(cs)
            }
        }

        test("$label: sums and products are always canonical (no trailing zero)") {
            checkAll(polys, polys) { p, q ->
                isCanonical(kit.addP(p, q)) shouldBe true
                isCanonical(kit.mulP(p, q)) shouldBe true
            }
        }

        test("$label: p + q agrees with the coefficient-wise model") {
            checkAll(polys, polys) { p, q ->
                kit.addP(p, q).coeffs shouldBe model.add(p.coeffs, q.coeffs)
            }
        }

        test("$label: p · q agrees with the convolution model") {
            checkAll(polys, polys) { p, q ->
                kit.mulP(p, q).coeffs shouldBe model.mul(p.coeffs, q.coeffs)
            }
        }

        test("$label: embed(c) is the constant c, and embed(0) is the zero polynomial") {
            checkAll(coeffArb) { c ->
                kit.embed(c).coeffs shouldBe model.trim(listOf(c))
            }
            kit.embed(coeff.zero).coeffs.shouldBeEmpty()
        }

        if (negA != null) {
            test("$label: -p negates every coefficient, and p + (-p) = 0") {
                checkAll(polys) { p ->
                    val np = kit.neg(p)
                    np.coeffs shouldBe model.neg(p.coeffs, negA)
                    isCanonical(np) shouldBe true
                    kit.addP(p, np) shouldBe kit.zeroP
                }
            }
        }
    }

    modelChecks("ℕ[x]", NaturalAlgebras.NaturalCommutativeSemiring,
        NaturalAlgebras.NaturalCommutativeSemiring.polyKit(NaturalAlgebras.eqNatural),
        ArbPoly.smallNatural, eqA = NaturalAlgebras.eqNatural)

    modelChecks("ℤ[x]", zz, zKit, ArbPoly.smallInteger, zz.add.inverse::invoke, eqA = eqZ)

    modelChecks("ℚ[x]", RationalAlgebras.RationalField, RationalAlgebras.RationalField.polyKit(),
        ArbPoly.smallRational, RationalAlgebras.RationalField.add.inverse::invoke)

    listOf(2, 5, 11).forEach { p ->
        val field = PrimeField(p.toBigInteger())
        modelChecks("𝔽_$p[x]", field, field.polyKit(),ArbPoly.residue(p), field.add.inverse::invoke)
    }

    val z4 = integersMod(4)
    modelChecks("ℤ/4[x]", z4, z4.polyKit(),ArbPoly.residue(4), z4.add.inverse::invoke)

    val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
    modelChecks("ℍ_ℚ[x]", hq, hq.polyKit(), ArbRationalQuaternion.smallRationalQuaternion, hq.add.inverse::invoke)

    /* --------------------------
     * Canonical form and monomials
     * -------------------------- */

    test("the zero polynomial is the empty coefficient list, however it is reached") {
        zKit.zeroP.coeffs.shouldBeEmpty()
        Poly.zero<java.math.BigInteger>() shouldBe zKit.zeroP
        ArbPoly.polyOf(zKit, ints(0, 0, 0)) shouldBe zKit.zeroP
        zKit.addP(zKit.x(), zKit.neg(zKit.x())) shouldBe zKit.zeroP
    }

    test("1 is [1] and x is [0, 1]") {
        zKit.oneP.coeffs shouldBe ints(1)
        zKit.x().coeffs shouldBe ints(0, 1)
    }

    test("x^m · x^n = x^(m+n), and x^n has n zeros then a 1") {
        checkAll(Arb.int(0..6), Arb.int(0..6)) { m, n ->
            zKit.mulP(zKit.xPow(m), zKit.xPow(n)) shouldBe zKit.xPow(m + n)
            zKit.xPow(n).coeffs shouldBe List(n) { 0.toBigInteger() } + 1.toBigInteger()
        }
    }

    /* --------------------------
     * Degrees
     * -------------------------- */

    test("ℤ[x]: deg(p + q) ≤ max(deg p, deg q)") {
        val polys = ArbPoly.poly(zKit, ArbPoly.smallInteger)
        checkAll(polys, polys) { p, q ->
            (zKit.addP(p, q).degree <= maxOf(p.degree, q.degree)) shouldBe true
        }
    }

    test("ℤ[x] is an integral domain: deg(pq) = deg p + deg q for p, q ≠ 0") {
        val polys = ArbPoly.poly(zKit, ArbPoly.smallInteger)
        checkAll(polys, polys) { p, q ->
            if (p != zKit.zeroP && q != zKit.zeroP) zKit.mulP(p, q).degree shouldBe p.degree + q.degree
        }
    }

    test("ℍ_ℚ[x]: no zero divisors either, so degrees still add") {
        val kit = hq.polyKit()
        val polys = ArbPoly.poly(kit, ArbRationalQuaternion.smallRationalQuaternion, maxDegree = 3)
        checkAll(polys, polys) { p, q ->
            if (p != kit.zeroP && q != kit.zeroP) kit.mulP(p, q).degree shouldBe p.degree + q.degree
        }
    }

    test("ℤ/4[x]: leading coefficients can cancel, so (2x)² = 0 and (2x + 1)² = 1") {
        val kit = z4.polyKit()
        val twoX = ArbPoly.polyOf(kit, ints(0, 2))
        val twoXPlusOne = ArbPoly.polyOf(kit, ints(1, 2))

        kit.mulP(twoX, twoX) shouldBe kit.zeroP
        // 2x + 1 is a unit of degree 1: it is its own inverse.
        kit.mulP(twoXPlusOne, twoXPlusOne) shouldBe kit.oneP
    }

    /* --------------------------
     * Worked examples
     * -------------------------- */

    test("ℤ[x]: (x + 1)² = x² + 2x + 1 and (x - 1)(x + 1) = x² - 1") {
        val xPlus1 = ArbPoly.polyOf(zKit, ints(1, 1))
        val xMinus1 = ArbPoly.polyOf(zKit, ints(-1, 1))

        zKit.mulP(xPlus1, xPlus1).coeffs shouldBe ints(1, 2, 1)
        zKit.mulP(xMinus1, xPlus1).coeffs shouldBe ints(-1, 0, 1)
    }

    test("ℤ[x]: (x - 1)(x⁴ + x³ + x² + x + 1) = x⁵ - 1") {
        val xMinus1 = ArbPoly.polyOf(zKit, ints(-1, 1))
        val phi5 = ArbPoly.polyOf(zKit, ints(1, 1, 1, 1, 1))

        zKit.mulP(xMinus1, phi5).coeffs shouldBe ints(-1, 0, 0, 0, 0, 1)
    }

    test("𝔽₂[x]: the freshman's dream (x + 1)² = x² + 1") {
        val kit = PrimeField(2.toBigInteger()).polyKit()
        val xPlus1 = ArbPoly.polyOf(kit, ints(1, 1))

        kit.mulP(xPlus1, xPlus1).coeffs shouldBe ints(1, 0, 1)
    }

    test("𝔽₅[x]: (x + 1)⁵ = x⁵ + 1") {
        val kit = PrimeField(5.toBigInteger()).polyKit()
        val xPlus1 = ArbPoly.polyOf(kit, ints(1, 1))
        val fifth = (1 until 5).fold(xPlus1) { acc, _ -> kit.mulP(acc, xPlus1) }

        fifth.coeffs shouldBe ints(1, 0, 0, 0, 0, 1)
    }

    test("ℍ_ℚ[x]: coefficients multiply in order, so (i·x)(j·x) = k·x² but (j·x)(i·x) = -k·x²") {
        val kit = hq.polyKit()
        val zero = Rational.ZERO
        val one = Rational.ONE
        val i = quaternion(zero, one, zero, zero)
        val j = quaternion(zero, zero, one, zero)
        val k = quaternion(zero, zero, zero, one)
        val ix = ArbPoly.polyOf(kit, listOf(hq.zero, i))
        val jx = ArbPoly.polyOf(kit, listOf(hq.zero, j))

        kit.mulP(ix, jx).coeffs shouldBe listOf(hq.zero, hq.zero, k)
        kit.mulP(jx, ix).coeffs shouldBe listOf(hq.zero, hq.zero, hq.add.inverse(k))
    }
})
