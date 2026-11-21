package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbLipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Property tests for the Gravesian octonions `CD⟨ℤ⟨i, j, k⟩⟩ ≅ ℤ⁸`.
 */
class GravesianOctonionSpec : FunSpec({
    lawTests(lawGraph("Gravesian octonions") {
        val ring = GravesianOctonionAlgebras.GravesianOctonionRing
        val eq = GravesianOctonionAlgebras.eqGravesianOctonion
        val pr = GravesianOctonionAlgebras.printableGravesianOctonion
        val arb = ArbGravesianOctonion.gravesianOctonion

        branch("GravesianOctonionRing satisfies NonAssociativeInvolutiveRingLaws") {
            suite(NonAssociativeInvolutiveRingLaws(
                algebra = ring,
                arb = arb,
                eq = eq,
                pr = pr
            ))
        }

        branch("Gravesian octonion multiplication is exactly alternative and Moufang") {
            suite(AlternativityLaw(ring.mul.op, arb, eq, pr))
            suite(MoufangLaw(ring.mul.op, arb, eq, pr))
        }

        law("normSq is multiplicative in ℤ: N(xy) = N(x)N(y)", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = IntegerAlgebras.IntegerCommutativeRing.mul.op,
                arbA = arb,
                eqN = IntegerAlgebras.eqInteger,
                prA = pr,
                prN = IntegerAlgebras.printableInteger
            ))

        branch("ZModuleGravesianOctonion satisfies RModuleLaws") {
            suite(RModuleLaws(
                module = GravesianOctonionAlgebras.ZModuleGravesianOctonion,
                scalarArb = ArbInteger.small,
                vectorArb = arb,
                eqR = IntegerAlgebras.eqInteger,
                eqM = eq,
                prR = IntegerAlgebras.printableInteger,
                prM = pr
            ))
        }

        local("the Cayley-Dickson basis is the standard ℤ-basis of ℤ⁸ and its imaginary units square to -1") {
            lawCheck(ring.basis.size == 8)
            ring.basis.drop(1).forEach { e ->
                lawCheck(eq(ring.mul(e, e), ring.negOne))
                lawCheck(ring.normSq(e) == java.math.BigInteger.ONE)
            }
        }

        local("the 16 Gravesian units ±1, ±e₁, …, ±e₇ are the invertible elements, with u⁻¹ = conj(u)") {
            ring.units.size shouldBe 16
            ring.units.forEach { u ->
                lawCheck(ring.hasReciprocal(u))
                lawCheck(eq(ring.reciprocal(u), ring.conj(u)))
                lawCheck(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
                lawCheck(eq(ring.mul(ring.reciprocal(u), u), ring.one))
            }
        }

        local("non-units have no reciprocal in the Gravesian octonions") {
            lawCheckAll(arb) { o ->
                if (ring.normSq(o) != java.math.BigInteger.ONE) {
                    lawCheck(!ring.hasReciprocal(o))
                    shouldThrow<ArithmeticException> { ring.reciprocal(o) }
                }
            }
        }

        local("all 168 embeddings Lip ↪ Gravesian are multiplicative") {
            val quaternions = LipschitzQuaternionAlgebras.LipschitzQuaternionRing
            checkAllQuaternionEmbeddingsMultiplicative(
                embeddings = GravesianOctonionAlgebras.allQuaternionEmbeddings(),
                quaternionRing = quaternions,
                quaternionBasis = quaternions.basis,
                octonionRing = ring,
                eqO = eq
            )
        }

        branch("the canonical Cayley-Dickson embedding Lip ↪ Gravesian is an injective ring homomorphism") {
            val mono = LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism
            suite(NonAssociativeRingHomomorphismLaws(
                hom = mono::invoke,
                domain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
                codomain = ring,
                arb = ArbLipschitzQuaternion.lipschitzQuaternion,
                eqB = eq,
                prA = LipschitzQuaternionAlgebras.printableLipschitzQuaternion,
                prB = pr
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = ArbLipschitzQuaternion.lipschitzQuaternion,
                eqA = LipschitzQuaternionAlgebras.eqLipschitzQuaternion,
                eqB = eq,
                prA = LipschitzQuaternionAlgebras.printableLipschitzQuaternion,
                prB = pr
            ))
        }

        branch("GravesianToRationalOctonionMonomorphism is an injective ring homomorphism") {
            val mono = GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism
            suite(NonAssociativeRingHomomorphismLaws(
                hom = mono::invoke,
                domain = ring,
                codomain = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra,
                arb = arb,
                eqB = RationalOctonionAlgebras.eqRationalOctonion,
                prA = pr,
                prB = RationalOctonionAlgebras.printableRationalOctonion
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = arb,
                eqA = eq,
                eqB = RationalOctonionAlgebras.eqRationalOctonion,
                prA = pr,
                prB = RationalOctonionAlgebras.printableRationalOctonion
            ))
        }

        local("Lip → Gravesian → 𝕆_ℚ agrees with LipschitzToRationalOctonionMonomorphism") {
            lawCheckAll(ArbLipschitzQuaternion.lipschitzQuaternion) { lq ->
                val composite = GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism(
                    LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism(lq)
                )
                lawCheck(
                    RationalOctonionAlgebras.eqRationalOctonion(
                        composite,
                        LipschitzQuaternionAlgebras.LipschitzToRationalOctonionMonomorphism(lq)
                    )
                )
            }
        }
    })
})
