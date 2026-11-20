package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.combinatorics.Factorial
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.laws.LawGraphBuilder
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
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
    val nnKit = nn.polyKit(NaturalAlgebras.eqNatural)

    fun LawGraphBuilder.zPoly(vararg cs: Int) = ArbPoly.polyOf(zKit, ints(*cs))

    /**
     * Model, linearity and Leibniz checks for one coefficient semiring.
     */
    fun <A : Any> LawGraphBuilder.derivativeChecks(
        label: String,
        coeff: Semiring<A>,
        kit: PolyKit<A>,
        coeffArb: Arb<A>,
        maxDegree: Int = 4,
        eqA: Eq<A> = Eq.default(),
    ) {
        val model = ListPolyModel(coeff, eqA)
        val polys = ArbPoly.poly(kit, coeffArb, maxDegree)

        local("$label: p' agrees with (p')ₖ = (k+1)·pₖ₊₁ and is canonical") {
            lawCheckAll(polys) { p ->
                val dp = kit.derivative(p)
                dp.coeffs shouldBe model.derivative(p.coeffs)
                (dp.coeffs.isEmpty() || !eqA(dp.coeffs.last(), coeff.zero)) shouldBe true
            }
        }

        local("$label: d/dx is additive, (p + q)' = p' + q'") {
            kit.run {
                lawCheckAll(polys, polys) { p, q ->
                    derivative(addP(p, q)) shouldBe addP(derivative(p), derivative(q))
                }
            }
        }

        local("$label: constants are killed and scale on either side, (c·p)' = c·p' and (p·c)' = p'·c") {
            kit.run {
                lawCheckAll(polys, coeffArb) { p, c ->
                    val cp = embed(c)
                    derivative(cp) shouldBe zeroP
                    derivative(mulP(cp, p)) shouldBe mulP(cp, derivative(p))
                    derivative(mulP(p, cp)) shouldBe mulP(derivative(p), cp)
                }
            }
        }

        local("$label: Leibniz rule (pq)' = p'q + pq', keeping the order of factors") {
            kit.run {
                lawCheckAll(polys, polys) { p, q ->
                    derivative(mulP(p, q)) shouldBe addP(
                        mulP(derivative(p), q),
                        mulP(p, derivative(q))
                    )
                }
            }
        }
    }

    lawTests(lawGraph("formal derivative") {

        derivativeChecks("ℕ[x]", nn, nnKit, ArbPoly.smallNatural, eqA = NaturalAlgebras.eqNatural)
        derivativeChecks("ℤ[x]", zz, zKit, ArbPoly.smallInteger, eqA = eqZ)
        derivativeChecks("𝔽₅[x]", PrimeField(5.toBigInteger()), PrimeField(5.toBigInteger()).polyKit(), ArbPoly.residue(5))
        derivativeChecks("ℤ/4[x]", integersMod(4), integersMod(4).polyKit(), ArbPoly.residue(4))
        derivativeChecks("ℍ_ℚ[x]", hq, hq.polyKit(), ArbRationalQuaternion.smallRationalQuaternion, 3)

        /* --------------------------
         * ℤ[x]
         * -------------------------- */

        local("d/dx xⁿ = n·xⁿ⁻¹, with 1' = 0 and x' = 1") {
            zKit.derivative(zKit.oneP) shouldBe zKit.zeroP
            zKit.derivative(zKit.zeroP) shouldBe zKit.zeroP
            zKit.derivative(zKit.x()) shouldBe zKit.oneP
            lawCheckAll(Arb.int(1..8)) { n ->
                zKit.derivative(zKit.xPow(n)) shouldBe zKit.mulP(zKit.embed(n.toBigInteger()), zKit.xPow(n - 1))
            }
        }

        local("(5x² + 3x + 1)' = 10x + 3") {
            zKit.derivative(zPoly(1, 3, 5)) shouldBe zPoly(3, 10)
        }

        local("the derivative lowers the degree by exactly one in characteristic 0") {
            lawCheckAll(zPolys) { p ->
                if (p.degree >= 1) zKit.derivative(p).degree shouldBe p.degree - 1
            }
        }

        local("chain rule: (p ∘ q)' = (p' ∘ q)·q'") {
            val small = ArbPoly.poly(zKit, ArbPoly.smallInteger, maxDegree = 2)
            lawCheckAll(zPolys, small) { p, q ->
                zKit.derivative(zKit.subst(p, q)) shouldBe
                    zKit.mulP(zKit.subst(zKit.derivative(p), q), zKit.derivative(q))
            }
        }

        local("Taylor at 0: the k-th derivative at 0 is k!·cₖ") {
            zKit.run {
                lawCheckAll(zPolys) { p ->
                    val cs = p.coeffs

                    // p, p', p'', …, p⁽ⁿ⁺¹⁾
                    val derivatives = generateSequence(p, ::derivative).take(cs.size + 1).toList()

                    cs.zip(derivatives).forEachIndexed { k, (c, d) ->
                        eval(d, BigInteger.ZERO) shouldBe Factorial(k) * c
                    }
                    derivatives.last() shouldBe zeroP
                }
            }
        }

        /* --------------------------
         * Positive characteristic
         * -------------------------- */

        local("𝔽₅[x]: (x⁵)' = 0, a non-constant polynomial with zero derivative") {
            PrimeField(5.toBigInteger()).polyKit().run {
                derivative(xPow(5)) shouldBe zeroP
                derivative(xPow(10)) shouldBe zeroP
                derivative(xPow(6)) shouldBe xPow(5)
            }
        }

        local("𝔽₅[x]: the 5th derivative of any polynomial is 0") {
            PrimeField(5.toBigInteger()).polyKit().run {
                lawCheckAll(ArbPoly.poly(this, ArbPoly.residue(5), maxDegree = 12)) { p ->
                    val fifth = (1..5).fold(p) { acc, _ -> derivative(acc) }
                    fifth shouldBe zeroP
                }
            }
        }

        local("ℤ/4[x]: (x⁴)' = 4x³ = 0 but (x³)' = 3x²") {
            integersMod(4).polyKit().run {
                derivative(xPow(4)) shouldBe zeroP
                derivative(xPow(3)).coeffs shouldBe ints(0, 0, 3)
            }
        }

        /* --------------------------
         * Towers
         * -------------------------- */

        local("ℤ[x][y]: d/dy treats x as a constant") {
            zKit.extend("y").run {
                val x = gen(0)
                val y = gen(1)
                val two = embed(zKit.embed(2.toBigInteger()))

                derivative(x) shouldBe zeroP
                derivative(y) shouldBe oneP
                // d/dy (x·y²) = 2xy
                derivative(mulP(x, mulP(y, y))) shouldBe mulP(two, mulP(x, y))
                // d/dy (x³ + y) = 1
                derivative(addP(mulP(x, mulP(x, x)), y)) shouldBe oneP
            }
        }

        local("ℤ[x][y]: Leibniz still holds in the tower") {
            zKit.extend("y").run {
                val inner = ArbPoly.poly(zKit, ArbPoly.smallInteger, maxDegree = 2)
                val polys = ArbPoly.poly(this, inner, maxDegree = 2)
                lawCheckAll(polys, polys) { p, q ->
                    derivative(mulP(p, q)) shouldBe addP(mulP(derivative(p), q), mulP(p, derivative(q)))
                }
            }
        }
    })
})
