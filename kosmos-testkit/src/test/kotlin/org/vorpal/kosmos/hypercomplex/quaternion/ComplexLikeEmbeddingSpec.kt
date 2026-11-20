package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
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
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.LawGraphBuilder
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianInt
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras
import org.vorpal.kosmos.numberfields.quadratic.GaussianInts
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import org.vorpal.kosmos.numberfields.quadratic.GaussianRats
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The ×6 families of embeddings of a complex-like ring into a quaternion-like ring, one per
 * [AxisSignEmbedding]: `i ↦ ±i, ±j, ±k`.
 *
 * Every member of every family is checked to be an injective unital ring homomorphism whose image of
 * the imaginary unit squares to `-1`. The exact families (over ℤ and ℚ) use strict equality.
 */
class ComplexLikeEmbeddingSpec : FunSpec({
    lawTests(lawGraph("ℂ-like embeddings into ℍ") {
        fun <A : Any, Q : Any> LawGraphBuilder.checkFamily(
            family: (AxisSignEmbedding) -> RingMonomorphism<A, Q>,
            domain: Ring<A>,
            codomain: Ring<Q>,
            imaginaryUnit: A,
            arb: Arb<A>,
            eqA: Eq<A> = Eq.default(),
            eqQ: Eq<Q> = Eq.default()
        ) {
            AxisSignEmbedding.all.forEach { emb ->
                val phi = family(emb)
                branch("$emb") {
                    suite(RingHomomorphismLaws(
                        hom = phi::invoke,
                        domain = domain,
                        codomain = codomain,
                        arb = arb,
                        eqB = eqQ
                    ))
                    law(injectivityLaw(hom = phi::invoke, arbA = arb, eqA = eqA, eqB = eqQ))
                }
            }

            local("six embeddings; the images of i square to -1, are distinct, and come in ± pairs") {
                AxisSignEmbedding.all.size shouldBe 6

                val images = AxisSignEmbedding.all.map { emb ->
                    val u = family(emb)(imaginaryUnit)
                    lawCheck(eqQ(codomain.mul(u, u), codomain.negOne)) { "$emb: the image of i does not square to -1" }
                    u
                }

                images.indices.forEach { a ->
                    images.indices.filter { b -> b != a }.forEach { b ->
                        lawCheck(!eqQ(images[a], images[b])) { "embeddings $a and $b send i to the same element" }
                    }
                }
                ImagAxis.entries.forEach { axis ->
                    val plus = family(AxisSignEmbedding(axis, Sign.PLUS))(imaginaryUnit)
                    val minus = family(AxisSignEmbedding(axis, Sign.MINUS))(imaginaryUnit)
                    lawCheck(eqQ(minus, codomain.add.inverse(plus)))
                }
            }
        }

        branch("ℤ[i] ↪ Lip: all six embeddings are injective unital ring homomorphisms") {
            checkFamily(
                family = GaussianIntAlgebras::gaussianIntToLipschitzQuaternionEmbedding,
                domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
                codomain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
                imaginaryUnit = GaussianInts.I,
                arb = ArbGaussianInt.small
            )
        }

        branch("ℤ[i] ↪ 𝓗: all six embeddings are injective unital ring homomorphisms") {
            checkFamily(
                family = GaussianIntAlgebras::gaussianIntToHurwitzQuaternionEmbedding,
                domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
                codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing,
                imaginaryUnit = GaussianInts.I,
                arb = ArbGaussianInt.small
            )
        }

        branch("ℤ[i] ↪ ℍ_ℚ: all six embeddings are injective unital ring homomorphisms") {
            checkFamily(
                family = GaussianIntAlgebras::gaussianIntToRationalQuaternionEmbedding,
                domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
                codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
                imaginaryUnit = GaussianInts.I,
                arb = ArbGaussianInt.small
            )
        }

        branch("ℚ(i) ↪ ℍ_ℚ: all six embeddings are injective unital ring homomorphisms") {
            checkFamily(
                family = GaussianRatAlgebras::gaussianRatToRationalQuaternionEmbedding,
                domain = GaussianRatAlgebras.GaussianRatField,
                codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
                imaginaryUnit = GaussianRats.I,
                arb = ArbGaussianRat.small
            )
        }

        branch("ℂ ↪ ℍ: all six embeddings are injective unital ring homomorphisms (approximately)") {
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

        local("the canonical member of each family is the Cayley-Dickson embedding a ↦ (a, 0)") {
            AxisSignEmbedding.canonical shouldBe AxisSignEmbedding(ImagAxis.I, Sign.PLUS)

            lawCheckAll(ArbGaussianInt.small) { z ->
                GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()(z) shouldBe Quaternion(CD(z.cd, GaussianInts.ZERO.cd))
            }
            lawCheckAll(ArbGaussianRat.small) { z ->
                GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()(z) shouldBe Quaternion(CD(z.cd, GaussianRats.ZERO.cd))
            }
            lawCheckAll(ArbComplex.boundedComplex) { z ->
                ComplexAlgebras.complexToQuaternionEmbedding()(z) shouldBe quaternion(z.re, z.im, 0.0, 0.0)
            }
        }

        local("ℤ[i] ↪ 𝓗 factors through the Lipschitz inclusion") {
            AxisSignEmbedding.all.forEach { emb ->
                val direct = GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(emb)
                val viaLipschitz = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(emb) andThen
                    LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism
                lawCheckAll(ArbGaussianInt.small) { z ->
                    direct(z) shouldBe viaLipschitz(z)
                }
            }
        }

        branch("each of the six embeddings ℂ ↪ ℍ makes ℍ a two-dimensional left ℂ-vector space") {
            local("one space per embedding, each of dimension 2") {
                QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.keys shouldBe AxisSignEmbedding.all.toSet()
                QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.values.forEach { space ->
                    space.dimension shouldBe 2
                }
            }
            QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.forEach { (embedding, space) ->
                branch("$embedding") {
                    suite(VectorSpaceLaws(
                        space = space,
                        scalarArb = ArbComplex.boundedComplex,
                        vectorArb = ArbQuaternion.quaternion,
                        eqF = ComplexAlgebras.eqComplex,
                        eqV = QuaternionAlgebras.eqQuaternion,
                        prF = ComplexAlgebras.printableComplexPretty,
                        prV = QuaternionAlgebras.printableQuaternionPretty
                    ))
                }
            }
        }

        local("the canonical left ℂ-vector space structure on ℍ is the one for i ↦ +i") {
            val canonical = QuaternionAlgebras.QuaternionLeftComplexVectorSpaceCanonical
            val viaMap = QuaternionAlgebras.QuaternionLeftComplexVectorSpacesAll.getValue(AxisSignEmbedding.canonical)
            lawCheckAll(ArbComplex.boundedComplex, ArbQuaternion.quaternion) { c, q ->
                lawCheck(QuaternionAlgebras.eqQuaternion(canonical.leftAction(c, q), viaMap.leftAction(c, q)))
            }
        }
    })
})
