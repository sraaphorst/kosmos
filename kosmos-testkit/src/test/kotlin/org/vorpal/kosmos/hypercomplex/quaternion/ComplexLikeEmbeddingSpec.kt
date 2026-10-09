package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.AxisSignEmbedding
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.ImagAxis
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianInt
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianInt
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

/**
 * The ×6 families of embeddings of a complex-like ring into a quaternion-like ring, one per
 * [AxisSignEmbedding]: `i ↦ ±i, ±j, ±k`.
 *
 * Every member of every family is checked to be an injective unital ring homomorphism whose image of
 * the imaginary unit squares to `-1`. The exact families (over ℤ and ℚ) use strict equality.
 */
class ComplexLikeEmbeddingSpec : StringSpec({

    suspend fun <A : Any, Q : Any> checkFamily(
        family: (AxisSignEmbedding) -> RingMonomorphism<A, Q>,
        domain: Ring<A>,
        codomain: Ring<Q>,
        imaginaryUnit: A,
        arb: Arb<A>,
        eqA: Eq<A> = Eq.default(),
        eqQ: Eq<Q> = Eq.default()
    ) {
        AxisSignEmbedding.all.size shouldBe 6

        val images = AxisSignEmbedding.all.map { emb ->
            val phi = family(emb)
            RingHomomorphismLaws(
                hom = phi::invoke,
                domain = domain,
                codomain = codomain,
                arb = arb,
                eqB = eqQ
            ).fullTest().throwIfFailed()
            injectivityLaw(hom = phi::invoke, arbA = arb, eqA = eqA, eqB = eqQ).test()

            val u = phi(imaginaryUnit)
            check(eqQ(codomain.mul(u, u), codomain.negOne)) { "$emb: the image of i does not square to -1" }
            u
        }

        // The six images of i are pairwise distinct, and come in three ± pairs.
        images.indices.forEach { a ->
            images.indices.filter { b -> b != a }.forEach { b ->
                check(!eqQ(images[a], images[b])) { "embeddings $a and $b send i to the same element" }
            }
        }
        ImagAxis.entries.forEach { axis ->
            val plus = family(AxisSignEmbedding(axis, Sign.PLUS))(imaginaryUnit)
            val minus = family(AxisSignEmbedding(axis, Sign.MINUS))(imaginaryUnit)
            check(eqQ(minus, codomain.add.inverse(plus)))
        }
    }

    "ℤ[i] ↪ Lip: all six embeddings are injective unital ring homomorphisms" {
        checkFamily(
            family = GaussianIntAlgebras::gaussianIntToLipschitzQuaternionEmbedding,
            domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
            codomain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
            imaginaryUnit = GaussianInt.I,
            arb = ArbGaussianInt.small
        )
    }

    "ℤ[i] ↪ 𝓗: all six embeddings are injective unital ring homomorphisms" {
        checkFamily(
            family = GaussianIntAlgebras::gaussianIntToHurwitzQuaternionEmbedding,
            domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
            codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing,
            imaginaryUnit = GaussianInt.I,
            arb = ArbGaussianInt.small
        )
    }

    "ℤ[i] ↪ ℍ_ℚ: all six embeddings are injective unital ring homomorphisms" {
        checkFamily(
            family = GaussianIntAlgebras::gaussianIntToRationalQuaternionEmbedding,
            domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
            codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            imaginaryUnit = GaussianInt.I,
            arb = ArbGaussianInt.small
        )
    }

    "ℚ(i) ↪ ℍ_ℚ: all six embeddings are injective unital ring homomorphisms" {
        checkFamily(
            family = GaussianRatAlgebras::gaussianRatToRationalQuaternionEmbedding,
            domain = GaussianRatAlgebras.GaussianRatField,
            codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            imaginaryUnit = GaussianRat.I,
            arb = ArbGaussianRat.small
        )
    }

    "ℂ ↪ ℍ: all six embeddings are injective unital ring homomorphisms (approximately)" {
        checkFamily(
            family = ComplexAlgebras::complexToQuaternionEmbedding,
            domain = ComplexAlgebras.ComplexField,
            codomain = QuaternionAlgebras.QuaternionDivisionRing,
            imaginaryUnit = ComplexAlgebras.i,
            arb = ArbComplex.boundedComplex,
            eqA = ComplexAlgebras.eqComplex,
            eqQ = QuaternionAlgebras.eqQuaternion
        )
    }

    "the canonical member of each family is the Cayley-Dickson embedding a ↦ (a, 0)" {
        AxisSignEmbedding.canonical shouldBe AxisSignEmbedding(ImagAxis.I, Sign.PLUS)

        checkAll(ArbGaussianInt.small) { z ->
            GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()(z) shouldBe Quaternion(CD(z.cd, GaussianInt.ZERO.cd))
        }
        checkAll(ArbGaussianRat.small) { z ->
            GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()(z) shouldBe Quaternion(CD(z.cd, GaussianRat.ZERO.cd))
        }
        checkAll(ArbComplex.boundedComplex) { z ->
            ComplexAlgebras.complexToQuaternionEmbedding()(z) shouldBe Quaternion(CD(z, ComplexAlgebras.ComplexField.zero))
        }
    }

    "ℤ[i] ↪ 𝓗 factors through the Lipschitz inclusion" {
        AxisSignEmbedding.all.forEach { emb ->
            val direct = GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(emb)
            val viaLipschitz = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(emb) andThen
                LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism
            checkAll(ArbGaussianInt.small) { z ->
                direct(z) shouldBe viaLipschitz(z)
            }
        }
    }

    "each of the six embeddings ℂ ↪ ℍ makes ℍ a two-dimensional left ℂ-vector space" {
        QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.keys shouldBe AxisSignEmbedding.all.toSet()

        QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.values.forEach { space ->
            space.dimension shouldBe 2
            VectorSpaceLaws(
                space = space,
                scalarArb = ArbComplex.boundedComplex,
                vectorArb = ArbQuaternion.quaternion,
                eqF = ComplexAlgebras.eqComplex,
                eqV = QuaternionAlgebras.eqQuaternion,
                prF = ComplexAlgebras.printableComplexPretty,
                prV = QuaternionAlgebras.printableQuaternionPretty
            ).test().throwIfFailed()
        }
    }

    "the canonical left ℂ-vector space structure on ℍ is the one for i ↦ +i" {
        val canonical = QuaternionAlgebras.QuaternionLeftComplexVectorSpaceCanonical
        val viaMap = QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.getValue(AxisSignEmbedding.canonical)
        checkAll(ArbComplex.boundedComplex, ArbQuaternion.quaternion) { c, q ->
            check(QuaternionAlgebras.eqQuaternion(canonical.leftAction(c, q), viaMap.leftAction(c, q)))
        }
    }
})
