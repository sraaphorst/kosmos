package org.vorpal.kosmos.hypercomplex.splitoctonion

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The real split-octonions `𝕆'`, checked up to approximate equality.
 *
 * The exact algebraic facts are in [SplitOctonionSpec].
 */
class RealSplitOctonionSpec : FunSpec({
    lawTests(lawGraph("𝕆′ over ℝ") {
        val ring = SplitOctonionAlgebras.RealSplitOctonionAlgebra
        val eq = SplitOctonionAlgebras.eqSplitOctonion
        val pr = SplitOctonionAlgebras.printableSplitOctonionPretty
        val arb = ArbSplitOctonion.smallSplitOctonion

        branch("RealSplitOctonionStarAlgebra satisfies NonAssociativeStarAlgebraLaws") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = SplitOctonionAlgebras.RealSplitOctonionStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = arb,
                eqR = RealAlgebras.eqRealApprox,
                eqA = eq,
                prR = RealAlgebras.printableRealPretty,
                prA = pr
            ))
        }

        branch("SplitOctonionRealVectorSpace satisfies VectorSpaceLaws and has dimension 8") {
            suite(VectorSpaceLaws(
                space = SplitOctonionAlgebras.SplitOctonionRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = arb,
                eqF = RealAlgebras.eqRealApprox,
                eqV = eq,
                prF = RealAlgebras.printableRealPretty,
                prV = pr
            ))
            local("dimension 8") {
                SplitOctonionAlgebras.SplitOctonionRealVectorSpace.dimension shouldBe 8
            }
        }

        branch("real split-octonion multiplication is alternative") {
            suite(AlternativityLaw(ring.mul.op, arb, eq, pr))
        }

        law("the real split norm is multiplicative", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = RealAlgebras.RealField.mul.op,
                arbA = arb,
                eqN = RealAlgebras.eqRealApprox,
                prA = pr,
                prN = RealAlgebras.printableRealPretty
            ))

        local("the real basis: e₁, e₂, e₃ square to -1 and e₄, …, e₇ square to +1") {
            ring.basis.size shouldBe 8
            ring.basis.subList(1, 4).forEach { e -> lawCheck(eq(ring.mul(e, e), ring.negOne)) }
            ring.basis.subList(4, 8).forEach { e -> lawCheck(eq(ring.mul(e, e), ring.one)) }
        }

        local("reciprocal: o·o⁻¹ = o⁻¹·o = 1 away from the null cone") {
            lawCheckAll(ArbSplitOctonion.unitSplitOctonion) { o ->
                val inverse = ring.reciprocal(o)
                lawCheck(eq(ring.mul(o, inverse), ring.one))
                lawCheck(eq(ring.mul(inverse, o), ring.one))
            }
        }

        branch("ℍ ↪ 𝕆' is an injective unital homomorphism onto the span of 1, e₁, e₂, e₃") {
            val mono = SplitOctonionAlgebras.QuaternionToSplitOctonionMonomorphism
            suite(NonAssociativeRingHomomorphismLaws(
                hom = mono::invoke,
                domain = QuaternionAlgebras.QuaternionDivisionRing,
                codomain = ring,
                arb = ArbQuaternion.quaternion,
                eqB = eq,
                prA = QuaternionAlgebras.printableQuaternionPretty,
                prB = pr
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = ArbQuaternion.quaternion,
                eqA = QuaternionAlgebras.eqQuaternion,
                eqB = eq,
                prA = QuaternionAlgebras.printableQuaternionPretty,
                prB = pr
            ))
            local("the basis goes to 1, e₁, e₂, e₃") {
                QuaternionAlgebras.QuaternionDivisionRing.basis.map(mono::invoke) shouldBe ring.basis.take(4)
            }
        }

        local("ℍ ↪ 𝕆' preserves the norm: the quaternions are the positive-definite half") {
            val mono = SplitOctonionAlgebras.QuaternionToSplitOctonionMonomorphism
            lawCheckAll(ArbQuaternion.quaternion) { q ->
                lawCheck(RealAlgebras.eqRealApprox(ring.normSq(mono(q)), QuaternionAlgebras.QuaternionDivisionRing.normSq(q)))
            }
        }
    })
})
