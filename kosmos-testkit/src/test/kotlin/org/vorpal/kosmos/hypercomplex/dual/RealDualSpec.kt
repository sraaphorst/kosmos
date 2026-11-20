package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The real dual numbers, checked up to approximate equality.
 *
 * The exact algebraic facts are in [DualRingSpec]; the floating-point reciprocal contract at the
 * boundary (`NaN`, `±∞`, tiny values, overflowing tangents) is in `ReciprocalPolicySpec`.
 */
class RealDualSpec : FunSpec({
    lawTests(lawGraph("dual numbers over ℝ") {
        val ring = DualAlgebras.RealDualRing
        val eq = DualAlgebras.eqDual
        val eqR = RealAlgebras.eqRealApprox
        val pr = DualAlgebras.printableDualPretty
        val arb = ArbDual.smallDual

        branch("RealDualRing satisfies CommutativeRingLaws") {
            suite(CommutativeRingLaws(ring, arb, eq, pr))
        }

        branch("RealDualStarAlgebra satisfies StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = DualAlgebras.RealDualStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = arb,
                eqR = eqR,
                eqA = eq,
                prR = RealAlgebras.printableRealPretty,
                prA = pr
            ))
        }

        branch("DualRealVectorSpace satisfies VectorSpaceLaws and has dimension 2") {
            suite(VectorSpaceLaws(
                space = DualAlgebras.DualRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = arb,
                eqF = eqR,
                eqV = eq,
                prF = RealAlgebras.printableRealPretty,
                prV = pr
            ))
            local("dimension 2") {
                DualAlgebras.DualRealVectorSpace.dimension shouldBe 2
            }
        }

        local("ε over the reals is 0 + 1ε and squares to zero") {
            DualAlgebras.epsilon shouldBe dual(0.0, 1.0)
            lawCheck(eq(ring.mul(DualAlgebras.epsilon, DualAlgebras.epsilon), ring.zero))
        }

        local("normSq is a² and equals x·conj(x)") {
            lawCheckAll(arb) { x ->
                lawCheck(eqR(ring.normSq(x), x.a * x.a))
                lawCheck(eq(ring.mul(x, ring.conj(x)), dual(x.a * x.a, 0.0)))
            }
        }

        local("reciprocal: x·x⁻¹ = 1, the formula a⁻¹ - (b/a²)ε, and (x⁻¹)⁻¹ = x") {
            lawCheckAll(ArbDual.unitDual) { x ->
                val inverse = ring.reciprocal(x)
                lawCheck(eq(ring.mul(x, inverse), ring.one))
                lawCheck(eq(inverse, dual(1.0 / x.a, -x.b / (x.a * x.a))))
                lawCheck(eq(ring.reciprocal(inverse), x))
            }
        }

        local("elements with zero primal part are not invertible") {
            lawCheckAll(ArbDual.nonUnitDual) { x ->
                ring.hasReciprocal(x) shouldBe false
                shouldThrow<ArithmeticException> { ring.reciprocal(x) }
            }
            ring.hasReciprocal(ring.zero) shouldBe false
            ring.hasReciprocal(ring.one) shouldBe true
        }

        branch("the matrix embedding is a unital ring homomorphism over the reals") {
            val mono = DualAlgebras.dualToRank2MatrixMonomorphism(RealAlgebras.RealField)
            suite(RingHomomorphismLaws(
                hom = mono::invoke,
                domain = ring,
                codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
                arb = arb,
                eqB = DenseMatAlgebras.liftEq(eqR),
                prA = pr,
                prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
            ))
        }

        local("strict equality distinguishes what approximate equality identifies") {
            val x = dual(1.0, 2.0)
            val y = dual(1.0 + 1e-12, 2.0)
            DualAlgebras.eqDual(x, y) shouldBe true
            DualAlgebras.eqDualStrict(x, y) shouldBe false
            DualAlgebras.eqDualStrict(x, x) shouldBe true
        }

        // Dual numbers print as linear combinations over the basis {1, ε}, exactly like complex
        // (basis {1, i}) and split-complex (basis {1, j}) numbers.
        local("printables: signs, dropped zero terms and the elided unit coefficient") {
            val eps = Symbols.EPSILON
            val plain = DualAlgebras.printableDualReal
            val pretty = DualAlgebras.printableDualPretty

            plain(dual(3.0, 4.0)) shouldBe "3.0 + 4.0$eps"
            pretty(dual(3.0, 4.0)) shouldBe "3 + 4$eps"
            plain(dual(-3.0, -4.0)) shouldBe "-3.0 - 4.0$eps"
            pretty(dual(-3.0, -4.0)) shouldBe "-3 - 4$eps"

            plain(ring.zero) shouldBe "0"
            plain(ring.one) shouldBe "1.0"
            pretty(ring.one) shouldBe "1"

            plain(DualAlgebras.epsilon) shouldBe eps
            plain(ring.add.inverse(DualAlgebras.epsilon)) shouldBe "-$eps"
            plain(dual(2.5, 1.0)) shouldBe "2.5 + $eps"
            DualAlgebras.printableDualStrict(dual(2.5, 1.0)).isNotEmpty() shouldBe true
        }
    })
})
