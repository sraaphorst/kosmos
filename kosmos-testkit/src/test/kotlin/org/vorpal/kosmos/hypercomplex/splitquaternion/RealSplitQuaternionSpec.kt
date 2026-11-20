package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.ArbSplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The real split-quaternions `ℍ'`, and the embeddings into the split-quaternions.
 *
 * The exact algebraic facts are in [SplitQuaternionRingSpec]; here the floating-point structures are
 * checked up to approximate equality.
 */
class RealSplitQuaternionSpec : FunSpec({
    lawTests(lawGraph("ℍ′ over ℝ") {
        val ring = SplitQuaternionAlgebras.RealSplitQuaternionRing
        val eq = SplitQuaternionAlgebras.eqSplitQuaternion
        val pr = SplitQuaternionAlgebras.printableSplitQuaternionPretty
        val arb = ArbSplitQuaternion.smallSplitQuaternion

        branch("RealSplitQuaternionStarAlgebra satisfies StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = SplitQuaternionAlgebras.RealSplitQuaternionStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = arb,
                eqR = RealAlgebras.eqRealApprox,
                eqA = eq,
                prR = RealAlgebras.printableRealPretty,
                prA = pr
            ))
        }

        branch("SplitQuaternionRealVectorSpace satisfies VectorSpaceLaws and has dimension 4") {
            suite(VectorSpaceLaws(
                space = SplitQuaternionAlgebras.SplitQuaternionRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = arb,
                eqF = RealAlgebras.eqRealApprox,
                eqV = eq,
                prF = RealAlgebras.printableRealPretty,
                prV = pr
            ))
            local("dimension 4") {
                SplitQuaternionAlgebras.SplitQuaternionRealVectorSpace.dimension shouldBe 4
            }
        }

        local("the real units satisfy i² = -1, j² = k² = +1, ij = k") {
            val (i, j, k) = Triple(SplitQuaternionAlgebras.i, SplitQuaternionAlgebras.j, SplitQuaternionAlgebras.k)
            lawCheck(eq(ring.mul(i, i), ring.negOne))
            lawCheck(eq(ring.mul(j, j), ring.one))
            lawCheck(eq(ring.mul(k, k), ring.one))
            lawCheck(eq(ring.mul(i, j), k))
            ring.basis shouldBe listOf(ring.one, i, j, k)
        }

        local("reciprocal: q·q⁻¹ = q⁻¹·q = 1 away from the null cone") {
            lawCheckAll(ArbSplitQuaternion.unitSplitQuaternion) { q ->
                val inverse = ring.reciprocal(q)
                lawCheck(eq(ring.mul(q, inverse), ring.one))
                lawCheck(eq(ring.mul(inverse, q), ring.one))
            }
        }

        branch("ℂ ↪ ℍ' is an injective unital ring homomorphism onto the plane {w + xi}") {
            val mono = SplitQuaternionAlgebras.ComplexToSplitQuaternionMonomorphism
            suite(RingHomomorphismLaws(
                hom = mono::invoke,
                domain = ComplexAlgebras.ComplexField,
                codomain = ring,
                arb = ArbComplex.boundedComplex,
                eqB = eq,
                prA = ComplexAlgebras.printableComplexPretty,
                prB = pr
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = ArbComplex.boundedComplex,
                eqA = ComplexAlgebras.eqComplex,
                eqB = eq,
                prA = ComplexAlgebras.printableComplexPretty,
                prB = pr
            ))
            local("the image is the plane {w + xi}") {
                lawCheckAll(ArbComplex.boundedComplex) { c ->
                    mono(c) shouldBe splitQuaternion(c.re, c.im, 0.0, 0.0)
                }
            }
        }

        branch("the split-complex numbers embed along j and along k (exactly, over ℚ)") {
            val q = RationalAlgebras.RationalField
            val arbSplitComplex = ArbSplitComplex.splitComplexOf(ArbSplitQuaternion.smallRational)
            listOf(
                "along j" to SplitQuaternionAlgebras.splitComplexToSplitQuaternionMonomorphism(q),
                "along k" to SplitQuaternionAlgebras.splitComplexToSplitQuaternionAlongK(q)
            ).forEach { (along, mono) ->
                branch(along) {
                    suite(RingHomomorphismLaws(
                        hom = mono::invoke,
                        domain = SplitComplexAlgebras.splitComplexRing(q),
                        codomain = SplitQuaternionAlgebras.splitQuaternionRing(q),
                        arb = arbSplitComplex
                    ))
                    law(injectivityLaw(hom = mono::invoke, arbA = arbSplitComplex))
                }
            }
        }

        branch("the scalar embedding ℚ ↪ SplitQuaternion<ℚ> is an injective unital ring homomorphism") {
            val q = RationalAlgebras.RationalField
            val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
            suite(RingHomomorphismLaws(
                hom = embed::invoke,
                domain = q,
                codomain = SplitQuaternionAlgebras.splitQuaternionRing(q),
                arb = ArbSplitQuaternion.smallRational
            ))
            law(injectivityLaw(hom = embed::invoke, arbA = ArbSplitQuaternion.smallRational))
        }
    })
})
