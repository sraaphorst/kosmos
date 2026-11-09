package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
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
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import java.math.BigInteger

/**
 * The formal derivative `d/dx (Σ cₖxᵏ) = Σ k·cₖxᵏ⁻¹`, where `k·c` is repeated addition, so it is
 * defined over any semiring. In a tower it differentiates in the last adjoined variable and treats
 * the coefficients (polynomials in the earlier variables) as constants.
 */
class PolyDerivativeSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val zKit = zz.polyKit(eqZ)
    val zPolys = ArbPoly.poly(zKit, ArbPoly.smallInteger)
    val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
    val nn = NaturalAlgebras.NaturalCommutativeSemiring

    fun zPoly(vararg cs: Int) = ArbPoly.polyOf(zKit, ints(*cs))

    /**
     * Model, linearity and Leibniz checks for one coefficient semiring.
     */
    fun <A : Any> derivativeChecks(
        label: String,
        coeff: Semiring<A>,
        kit: PolyKit<A>,
        eqA: Eq<A>,
        coeffArb: Arb<A>,
        maxDegree: Int = 4
    ) {
        val model = ListPolyModel(coeff, eqA)
        val polys = ArbPoly.poly(kit, coeffArb, maxDegree)

        test("$label: p' agrees with (p')ₖ = (k+1)·pₖ₊₁ and is canonical") {
            checkAll(polys) { p ->
                val dp = kit.derivative(p)
                dp.coeffs shouldBe model.derivative(p.coeffs)
                (dp.coeffs.isEmpty() || !eqA(dp.coeffs.last(), coeff.zero)) shouldBe true
            }
        }

        test("$label: d/dx is additive, (p + q)' = p' + q'") {
            checkAll(polys, polys) { p, q ->
                kit.derivative(kit.addP(p, q)) shouldBe kit.addP(kit.derivative(p), kit.derivative(q))
            }
        }

        test("$label: constants are killed and scale on either side, (c·p)' = c·p' and (p·c)' = p'·c") {
            checkAll(polys, coeffArb) { p, c ->
                val cp = kit.embed(c)
                kit.derivative(cp) shouldBe kit.zeroP
                kit.derivative(kit.mulP(cp, p)) shouldBe kit.mulP(cp, kit.derivative(p))
                kit.derivative(kit.mulP(p, cp)) shouldBe kit.mulP(kit.derivative(p), cp)
            }
        }

        test("$label: Leibniz rule (pq)' = p'q + pq', keeping the order of factors") {
            checkAll(polys, polys) { p, q ->
                kit.derivative(kit.mulP(p, q)) shouldBe kit.addP(
                    kit.mulP(kit.derivative(p), q),
                    kit.mulP(p, kit.derivative(q))
                )
            }
        }
    }

    derivativeChecks("ℕ[x]", nn, nn.polyKit(NaturalAlgebras.eqNatural), NaturalAlgebras.eqNatural, ArbPoly.smallNatural)
    derivativeChecks("ℤ[x]", zz, zKit, eqZ, ArbPoly.smallInteger)
    derivativeChecks("𝔽₅[x]", PrimeField(5.toBigInteger()),
        PrimeField(5.toBigInteger()).polyKit(Eq.default()), Eq.default(), ArbPoly.residue(5))
    derivativeChecks("ℤ/4[x]", integersMod(4), integersMod(4).polyKit(Eq.default()), Eq.default(), ArbPoly.residue(4))
    derivativeChecks("ℍ_ℚ[x]", hq, hq.polyKit(Eq.default()), Eq.default(), ArbPoly.smallQuaternion, maxDegree = 3)

    /* --------------------------
     * ℤ[x]
     * -------------------------- */

    test("d/dx xⁿ = n·xⁿ⁻¹, with 1' = 0 and x' = 1") {
        zKit.derivative(zKit.oneP) shouldBe zKit.zeroP
        zKit.derivative(zKit.zeroP) shouldBe zKit.zeroP
        zKit.derivative(zKit.x()) shouldBe zKit.oneP
        checkAll(Arb.int(1..8)) { n ->
            zKit.derivative(zKit.xPow(n)) shouldBe zKit.mulP(zKit.embed(n.toBigInteger()), zKit.xPow(n - 1))
        }
    }

    test("(5x² + 3x + 1)' = 10x + 3") {
        zKit.derivative(zPoly(1, 3, 5)) shouldBe zPoly(3, 10)
    }

    test("the derivative lowers the degree by exactly one in characteristic 0") {
        checkAll(zPolys) { p ->
            if (p.degree >= 1) zKit.derivative(p).degree shouldBe p.degree - 1
        }
    }

    test("chain rule: (p ∘ q)' = (p' ∘ q)·q'") {
        val small = ArbPoly.poly(zKit, ArbPoly.smallInteger, maxDegree = 2)
        checkAll(zPolys, small) { p, q ->
            zKit.derivative(zKit.subst(p, q)) shouldBe
                zKit.mulP(zKit.subst(zKit.derivative(p), q), zKit.derivative(q))
        }
    }

    test("Taylor at 0: the k-th derivative at 0 is k!·cₖ") {
        checkAll(zPolys) { p ->
            var d = p
            var factorial = BigInteger.ONE
            p.coeffs.indices.forEach { k ->
                if (k > 0) factorial *= k.toBigInteger()
                zKit.eval(d, BigInteger.ZERO) shouldBe factorial * p.coeffs[k]
                d = zKit.derivative(d)
            }
            d shouldBe zKit.zeroP
        }
    }

    /* --------------------------
     * Positive characteristic
     * -------------------------- */

    test("𝔽₅[x]: (x⁵)' = 0, a non-constant polynomial with zero derivative") {
        val kit = PrimeField(5.toBigInteger()).polyKit(Eq.default())
        kit.derivative(kit.xPow(5)) shouldBe kit.zeroP
        kit.derivative(kit.xPow(10)) shouldBe kit.zeroP
        kit.derivative(kit.xPow(6)) shouldBe kit.xPow(5)
    }

    test("𝔽₅[x]: the 5th derivative of any polynomial is 0") {
        val kit = PrimeField(5.toBigInteger()).polyKit(Eq.default())
        checkAll(ArbPoly.poly(kit, ArbPoly.residue(5), maxDegree = 12)) { p ->
            val fifth = (1..5).fold(p) { acc, _ -> kit.derivative(acc) }
            fifth shouldBe kit.zeroP
        }
    }

    test("ℤ/4[x]: (x⁴)' = 4x³ = 0 but (x³)' = 3x²") {
        val kit = integersMod(4).polyKit(Eq.default())
        kit.derivative(kit.xPow(4)) shouldBe kit.zeroP
        kit.derivative(kit.xPow(3)).coeffs shouldBe ints(0, 0, 3)
    }

    /* --------------------------
     * Towers
     * -------------------------- */

    test("ℤ[x][y]: d/dy treats x as a constant") {
        val zxy = zKit.extend("y")
        val x = zxy.gen(0)
        val y = zxy.gen(1)
        val two = zxy.embed(zKit.embed(2.toBigInteger()))

        zxy.derivative(x) shouldBe zxy.zeroP
        zxy.derivative(y) shouldBe zxy.oneP
        // d/dy (x·y²) = 2xy
        zxy.derivative(zxy.mulP(x, zxy.mulP(y, y))) shouldBe zxy.mulP(two, zxy.mulP(x, y))
        // d/dy (x³ + y) = 1
        zxy.derivative(zxy.addP(zxy.mulP(x, zxy.mulP(x, x)), y)) shouldBe zxy.oneP
    }

    test("ℤ[x][y]: Leibniz still holds in the tower") {
        val zxy = zKit.extend("y")
        val inner = ArbPoly.poly(zKit, ArbPoly.smallInteger, maxDegree = 2)
        val polys = ArbPoly.poly(zxy, inner, maxDegree = 2)
        checkAll(polys, polys) { p, q ->
            zxy.derivative(zxy.mulP(p, q)) shouldBe zxy.addP(
                zxy.mulP(zxy.derivative(p), q),
                zxy.mulP(p, zxy.derivative(q))
            )
        }
    }
})
