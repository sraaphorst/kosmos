package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Property tests for the rational octonions `𝕆_ℚ = CD⟨ℍ_ℚ⟩`.
 *
 * Arithmetic is exact, so alternativity, the Moufang identities and multiplicativity of the norm
 * are verified with strict equality: these are the exact algebraic facts that the floating-point
 * octonion specs can only check approximately.
 */
class RationalOctonionSpec : StringSpec({
    val ring = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra
    val eq = RationalOctonionAlgebras.eqRationalOctonion
    val pr = RationalOctonionAlgebras.printableRationalOctonion
    val arb = ArbRationalOctonion.smallRationalOctonion

    "RationalOctonionDivisionAlgebra satisfies NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(
            algebra = ring,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }

    "rational octonion multiplication is exactly alternative" {
        AlternativityLaw(ring.mul.op, arb, eq, pr).test()
    }

    "rational octonion multiplication exactly satisfies the Moufang identities" {
        MoufangLaw(ring.mul.op, arb, eq, pr).test()
    }

    "rational octonion multiplication is not associative: (e₁e₂)e₄ = -e₁(e₂e₄)" {
        val (e1, e2, e4) = listOf(1, 2, 4).map(ring.basisMap::getValue)
        val left = ring.mul(ring.mul(e1, e2), e4)
        val right = ring.mul(e1, ring.mul(e2, e4))
        check(!eq(left, right))
        check(eq(left, ring.add.inverse(right)))
    }

    "normSq is exactly multiplicative: N(xy) = N(x)N(y)" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = ring.normSq,
            mulN = RationalAlgebras.RationalField.mul.op,
            arbA = arb,
            eqN = RationalAlgebras.eqRational,
            prA = pr,
            prN = RationalAlgebras.printableRational
        ).test()
    }

    "the Cayley-Dickson basis units e₁, …, e₇ all square to -1" {
        ring.basis.drop(1).forEach { e ->
            check(eq(ring.mul(e, e), ring.negOne))
        }
    }

    "RationalOctonionVectorSpace satisfies VectorSpaceLaws" {
        VectorSpaceLaws(
            space = RationalOctonionAlgebras.RationalOctonionVectorSpace,
            scalarArb = ArbRational.small,
            vectorArb = arb,
            eqF = RationalAlgebras.eqRational,
            eqV = eq,
            prF = RationalAlgebras.printableRational,
            prV = pr
        ).fullTest().throwIfFailed()
    }

    "all 168 embeddings ℍ_ℚ ↪ 𝕆_ℚ are multiplicative" {
        val quaternions = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        checkAllQuaternionEmbeddingsMultiplicative(
            embeddings = RationalOctonionAlgebras.allQuaternionEmbeddings(),
            quaternionRing = quaternions,
            quaternionBasis = quaternions.basis,
            octonionRing = ring,
            eqO = eq
        )
    }

    "the canonical Cayley-Dickson embedding ℍ_ℚ ↪ 𝕆_ℚ is an injective ring homomorphism" {
        val mono = RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            codomain = ring,
            arb = ArbRationalQuaternion.smallRationalQuaternion,
            eqB = eq,
            prA = RationalQuaternionAlgebras.printableRationalQuaternion,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = ArbRationalQuaternion.smallRationalQuaternion,
            eqA = RationalQuaternionAlgebras.eqRationalQuaternion,
            eqB = eq,
            prA = RationalQuaternionAlgebras.printableRationalQuaternion,
            prB = pr
        ).test()
    }

    "the conversion 𝕆_ℚ → 𝕆 is a ring homomorphism up to floating-point rounding" {
        NonAssociativeRingHomomorphismLaws(
            hom = RationalOctonionAlgebras.rationalOctonionToOctonionApprox::invoke,
            domain = ring,
            codomain = OctonionAlgebras.OctonionDivisionAlgebra,
            arb = arb,
            eqB = OctonionAlgebras.eqOctonion,
            prA = pr,
            prB = OctonionAlgebras.printableOctonionPretty
        ).fullTest().throwIfFailed()
    }

    "RationalOctonionDivisionAlgebra satisfies NormedDivisionAlgebraLaws exactly" {
        NormedDivisionAlgebraLaws(
            algebra = ring,
            arbA = ArbRationalOctonion.nonZeroRationalOctonion,
            mulN = RationalAlgebras.RationalField.mul.op,
            eqA = eq,
            eqN = RationalAlgebras.eqRational,
            prA = pr,
            prN = RationalAlgebras.printableRational,
            zeroN = Option.Some(Rational.ZERO)
        ).fullTest().throwIfFailed()
    }

    "the reciprocal in 𝕆_ℚ is conj(x)/N(x), and x · x⁻¹ = x⁻¹ · x = 1 exactly" {
        checkAll(ArbRationalOctonion.nonZeroRationalOctonion) { x ->
            val inverse = ring.reciprocal(x)
            val expected = RationalOctonionAlgebras.RationalOctonionVectorSpace.leftAction(
                ring.normSq(x).reciprocal(),
                ring.conj(x)
            )
            check(eq(inverse, expected))
            check(eq(ring.mul(x, inverse), ring.one))
            check(eq(ring.mul(inverse, x), ring.one))
        }
    }

    "the inverse property holds despite nonassociativity: x⁻¹(xy) = y = (yx)x⁻¹" {
        checkAll(ArbRationalOctonion.nonZeroRationalOctonion, ArbRationalOctonion.smallRationalOctonion) { x, y ->
            val inv = ring.reciprocal(x)
            check(eq(ring.mul(inv, ring.mul(x, y)), y))
            check(eq(ring.mul(ring.mul(y, x), inv), y))
        }
    }

    "zero has no reciprocal" {
        check(!ring.hasReciprocal(ring.zero))
        shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
    }
})
