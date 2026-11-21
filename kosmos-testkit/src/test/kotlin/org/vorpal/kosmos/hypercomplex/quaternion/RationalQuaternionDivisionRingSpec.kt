package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.laws.algebra.DivisionRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Property tests for the rational quaternions `ℍ_ℚ = CD⟨ℚ(i)⟩`.
 *
 * All arithmetic here is exact, so every law is checked with strict equality: this is the level of
 * the tower at which the Cayley-Dickson construction can be verified without floating-point noise.
 */
class RationalQuaternionDivisionRingSpec : FunSpec({
    lawTests(lawGraph("ℍ over ℚ") {
        val ring = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val eq = RationalQuaternionAlgebras.eqRationalQuaternion
        val pr = RationalQuaternionAlgebras.printableRationalQuaternion

        branch("RationalQuaternionDivisionRing satisfies DivisionRingLaws") {
            suite(DivisionRingLaws(
                ring = ring,
                arb = ArbRationalQuaternion.nonZeroRationalQuaternion,
                eq = eq,
                pr = pr
            ))
        }

        branch("RationalQuaternionDivisionRing satisfies InvolutiveRingLaws") {
            suite(InvolutiveRingLaws(
                ring = ring,
                arb = ArbRationalQuaternion.rationalQuaternion,
                eq = eq,
                pr = pr
            ))
        }

        branch("RationalQuaternionDivisionRing satisfies NormedDivisionAlgebraLaws exactly") {
            suite(NormedDivisionAlgebraLaws(
                algebra = ring,
                arbA = ArbRationalQuaternion.nonZeroRationalQuaternion,
                mulN = RationalAlgebras.RationalField.mul.op,
                eqA = eq,
                eqN = RationalAlgebras.eqRational,
                prA = pr,
                prN = RationalAlgebras.printableRational,
                zeroN = Option.Some(Rational.ZERO)
            ))
        }

        branch("RationalQuaternionVectorSpace satisfies VectorSpaceLaws") {
            suite(VectorSpaceLaws(
                space = RationalQuaternionAlgebras.RationalQuaternionVectorSpace,
                scalarArb = ArbRational.small,
                vectorArb = ArbRationalQuaternion.rationalQuaternion,
                eqF = RationalAlgebras.eqRational,
                eqV = eq,
                prF = RationalAlgebras.printableRational,
                prV = pr
            ))
        }

        local("the basis 1, i, j, k satisfies Hamilton's relations i² = j² = k² = ijk = -1") {
            val (_, i, j, k) = ring.basis
            val negOne = ring.negOne
            lawCheck(eq(ring.mul(i, i), negOne))
            lawCheck(eq(ring.mul(j, j), negOne))
            lawCheck(eq(ring.mul(k, k), negOne))
            lawCheck(eq(ring.mul(ring.mul(i, j), k), negOne))
            lawCheck(eq(ring.mul(i, j), k) && eq(ring.mul(j, i), ring.add.inverse(k)))
        }

        local("reciprocal is conj(q) / N(q)") {
            lawCheckAll(ArbRationalQuaternion.nonZeroRationalQuaternion) { q ->
                val expected = RationalQuaternionAlgebras.RationalQuaternionVectorSpace.leftAction(
                    ring.normSq(q).reciprocal(),
                    ring.conj(q)
                )
                lawCheck(eq(ring.reciprocal(q), expected))
            }
        }

        branch("the conversion ℍ_ℚ → ℍ satisfies RingHomomorphismLaws up to floating-point rounding") {
            suite(RingHomomorphismLaws(
                hom = RationalQuaternionAlgebras.rationalQuaternionToQuaternionApprox::invoke,
                domain = ring,
                codomain = QuaternionAlgebras.QuaternionDivisionRing,
                arb = ArbRationalQuaternion.smallRationalQuaternion,
                eqB = QuaternionAlgebras.eqQuaternion,
                prA = pr,
                prB = QuaternionAlgebras.printableQuaternionPretty
            ))
        }
    })
})
