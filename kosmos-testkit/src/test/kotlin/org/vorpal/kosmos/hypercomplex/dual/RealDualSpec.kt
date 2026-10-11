package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras

/**
 * The real dual numbers, checked up to approximate equality.
 *
 * The exact algebraic facts are in [DualRingSpec]; the floating-point reciprocal contract at the
 * boundary (`NaN`, `±∞`, tiny values, overflowing tangents) is in `ReciprocalPolicySpec`.
 */
class RealDualSpec : StringSpec({
    val ring = DualAlgebras.RealDualRing
    val eq = DualAlgebras.eqDual
    val eqR = RealAlgebras.eqRealApprox
    val pr = DualAlgebras.printableDualPretty
    val arb = ArbDual.smallDual

    "RealDualRing satisfies CommutativeRingLaws" {
        CommutativeRingLaws(ring, arb, eq, pr).fullTest().throwIfFailed()
    }

    "RealDualStarAlgebra satisfies StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = DualAlgebras.RealDualStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = arb,
            eqR = eqR,
            eqA = eq,
            prR = RealAlgebras.printableRealPretty,
            prA = pr
        ).fullTest().throwIfFailed()
    }

    "DualRealVectorSpace satisfies VectorSpaceLaws and has dimension 2" {
        DualAlgebras.DualRealVectorSpace.dimension shouldBe 2
        VectorSpaceLaws(
            space = DualAlgebras.DualRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = arb,
            eqF = eqR,
            eqV = eq,
            prF = RealAlgebras.printableRealPretty,
            prV = pr
        ).test().throwIfFailed()
    }

    "ε over the reals is 0 + 1ε and squares to zero" {
        DualAlgebras.epsilon shouldBe dual(0.0, 1.0)
        check(eq(ring.mul(DualAlgebras.epsilon, DualAlgebras.epsilon), ring.zero))
    }

    "normSq is a² and equals x·conj(x)" {
        checkAll(arb) { x ->
            check(eqR(ring.normSq(x), x.a * x.a))
            check(eq(ring.mul(x, ring.conj(x)), dual(x.a * x.a, 0.0)))
        }
    }

    "reciprocal: x·x⁻¹ = 1, the formula a⁻¹ - (b/a²)ε, and (x⁻¹)⁻¹ = x" {
        checkAll(ArbDual.unitDual) { x ->
            val inverse = ring.reciprocal(x)
            check(eq(ring.mul(x, inverse), ring.one))
            check(eq(inverse, dual(1.0 / x.a, -x.b / (x.a * x.a))))
            check(eq(ring.reciprocal(inverse), x))
        }
    }

    "elements with zero primal part are not invertible" {
        checkAll(ArbDual.nonUnitDual) { x ->
            ring.hasReciprocal(x) shouldBe false
            shouldThrow<ArithmeticException> { ring.reciprocal(x) }
        }
        ring.hasReciprocal(ring.zero) shouldBe false
        ring.hasReciprocal(ring.one) shouldBe true
    }

    "the matrix embedding is a unital ring homomorphism over the reals" {
        val mono = DualAlgebras.dualToRank2MatrixMonomorphism(RealAlgebras.RealField)
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
            arb = arb,
            eqB = DenseMatAlgebras.liftEq(eqR),
            prA = pr,
            prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
        ).fullTest().throwIfFailed()
    }

    "strict equality distinguishes what approximate equality identifies" {
        val x = dual(1.0, 2.0)
        val y = dual(1.0 + 1e-12, 2.0)
        DualAlgebras.eqDual(x, y) shouldBe true
        DualAlgebras.eqDualStrict(x, y) shouldBe false
        DualAlgebras.eqDualStrict(x, x) shouldBe true
    }

    // Dual numbers print as linear combinations over the basis {1, ε}, exactly like complex
    // (basis {1, i}) and split-complex (basis {1, j}) numbers.
    "printables: signs, dropped zero terms and the elided unit coefficient" {
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
