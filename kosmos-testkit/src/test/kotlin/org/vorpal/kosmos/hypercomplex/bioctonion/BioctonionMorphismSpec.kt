package org.vorpal.kosmos.hypercomplex.bioctonion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.hypercomplex.biquaternion.ArbBiquaternion
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.octonion.ArbOctonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.checkAllQuaternionEmbeddingsMultiplicative
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw

/**
 * The maps into the bioctonions: the scalars ℂ, the two real forms 𝕆 and 𝕆', and the biquaternions.
 */
class BioctonionMorphismSpec : StringSpec({
    val ring = BioctonionAlgebras.BioctonionAlgebra
    val eq = BioctonionAlgebras.eqBioctonion
    val pr = BioctonionAlgebras.printableBioctonionPretty
    val complexField = ComplexAlgebras.ComplexField
    val eqC = ComplexAlgebras.eqComplex
    val prC = ComplexAlgebras.printableComplexPretty

    "ℂ ↪ 𝕆 ⊗ ℂ is an injective unital homomorphism" {
        val mono = BioctonionAlgebras.ComplexToBioctonionMonomorphism
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = complexField,
            codomain = ring,
            arb = ArbBiquaternion.smallComplex,
            eqB = eq,
            prA = prC,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(mono::invoke, ArbBiquaternion.smallComplex, eqC, eq, prC, pr).test()
        check(eq(mono(ComplexAlgebras.i), ring.h))
    }

    "𝕆 ↪ 𝕆 ⊗ ℂ is an injective unital homomorphism onto the fixed ring of the complex conjugate" {
        val mono = BioctonionAlgebras.OctonionToBioctonionMonomorphism
        val octonions = OctonionAlgebras.OctonionDivisionAlgebra
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = octonions,
            codomain = ring,
            arb = ArbOctonion.octonion,
            eqB = eq,
            prA = OctonionAlgebras.printableOctonionPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbOctonion.octonion, OctonionAlgebras.eqOctonion, eq,
            OctonionAlgebras.printableOctonionPretty, pr
        ).test()

        octonions.basis.map(mono::invoke) shouldBe ring.basis

        checkAll(ArbOctonion.octonion) { o ->
            check(eq(ring.complexConj(mono(o)), mono(o)))
            check(eq(ring.hermitianConj(mono(o)), mono(octonions.conj(o))))
            check(eqC(ring.normSq(mono(o)), complex(octonions.normSq(o), 0.0)))
        }
    }

    "𝕆' ↪ 𝕆 ⊗ ℂ is an injective unital homomorphism: e₄, …, e₇ ↦ h·e₄, …, h·e₇" {
        val mono = BioctonionAlgebras.SplitOctonionToBioctonionMonomorphism
        val split = SplitOctonionAlgebras.RealSplitOctonionAlgebra
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = split,
            codomain = ring,
            arb = ArbSplitOctonion.smallSplitOctonion,
            eqB = eq,
            prA = SplitOctonionAlgebras.printableSplitOctonionPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbSplitOctonion.smallSplitOctonion, SplitOctonionAlgebras.eqSplitOctonion, eq,
            SplitOctonionAlgebras.printableSplitOctonionPretty, pr
        ).test()

        (1..3).forEach { n -> check(eq(mono(split.basis[n]), ring.basis[n])) }
        (4..7).forEach { n -> check(eq(mono(split.basis[n]), ring.mul(ring.h, ring.basis[n]))) }

        checkAll(ArbSplitOctonion.smallSplitOctonion) { o ->
            check(eqC(ring.normSq(mono(o)), complex(split.normSq(o), 0.0)))
        }
    }

    "ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ (canonical Cayley-Dickson) is an injective unital homomorphism" {
        val mono = BioctonionAlgebras.BiquaternionToBioctonionMonomorphism
        val biquaternions = BiquaternionAlgebras.BiquaternionRing
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = biquaternions,
            codomain = ring,
            arb = ArbBiquaternion.biquaternion,
            eqB = eq,
            prA = BiquaternionAlgebras.printableBiquaternionPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbBiquaternion.biquaternion, BiquaternionAlgebras.eqBiquaternion, eq,
            BiquaternionAlgebras.printableBiquaternionPretty, pr
        ).test()

        biquaternions.basis.map(mono::invoke) shouldBe ring.basis.take(4)
        check(eq(mono(biquaternions.h), ring.h))

        checkAll(ArbBiquaternion.biquaternion) { q ->
            check(eq(mono(biquaternions.conj(q)), ring.conj(mono(q))))
            check(eqC(ring.normSq(mono(q)), biquaternions.normSq(q)))
        }
    }

    "all 168 signed-basis-unit embeddings ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ are multiplicative" {
        checkAllQuaternionEmbeddingsMultiplicative(
            embeddings = BioctonionAlgebras.allBiquaternionEmbeddings(),
            quaternionRing = BiquaternionAlgebras.BiquaternionRing,
            quaternionBasis = BiquaternionAlgebras.BiquaternionRing.basis,
            octonionRing = ring,
            eqO = eq
        )
    }

    "the 84 normalized embeddings are multiplicative, and the embeddings are ℂ-linear" {
        val normalized = BioctonionAlgebras.normalizedBiquaternionEmbeddings()
        checkAllQuaternionEmbeddingsMultiplicative(
            embeddings = normalized,
            quaternionRing = BiquaternionAlgebras.BiquaternionRing,
            quaternionBasis = BiquaternionAlgebras.BiquaternionRing.basis,
            octonionRing = ring,
            eqO = eq,
            expectedCount = 84
        )

        val (_, phi) = normalized.entries.first().toPair()
        check(eq(phi(BiquaternionAlgebras.BiquaternionRing.h), ring.h))
    }

    "the canonical member of the 168 is the Cayley-Dickson embedding q ↦ (q, 0)" {
        val (spec, phi) = BioctonionAlgebras.embeddingKit.createEmbedding(1, 2, Sign.PLUS, Sign.PLUS)
        spec.k shouldBe 3
        spec.kSign shouldBe 1
        checkAll(ArbBiquaternion.biquaternion) { q ->
            check(eq(phi(q), BioctonionAlgebras.BiquaternionToBioctonionMonomorphism(q)))
        }
    }
})
