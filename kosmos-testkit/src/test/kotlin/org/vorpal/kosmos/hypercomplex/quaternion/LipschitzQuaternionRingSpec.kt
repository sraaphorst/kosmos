package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Property tests for the Lipschitz quaternions `ℤ⟨i, j, k⟩ = CD⟨ℤ[i]⟩` and their inclusions
 * `ℤ⟨i, j, k⟩ ⊂ 𝓗 ⊂ ℍ_ℚ → ℍ`.
 */
class LipschitzQuaternionRingSpec : FunSpec({
    lawTests(lawGraph("Lipschitz quaternions") {
        val ring = LipschitzQuaternionAlgebras.LipschitzQuaternionRing
        val eq = LipschitzQuaternionAlgebras.eqLipschitzQuaternion
        val pr = LipschitzQuaternionAlgebras.printableLipschitzQuaternion
        val arb = ArbLipschitzQuaternion.lipschitzQuaternion

        branch("LipschitzQuaternionRing satisfies InvolutiveRingLaws") {
            suite(InvolutiveRingLaws(
                ring = ring,
                arb = arb,
                eq = eq,
                pr = pr
            ))
        }

        law("normSq is multiplicative: N(xy) = N(x)N(y) in ℤ", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = IntegerAlgebras.IntegerCommutativeRing.mul.op,
                arbA = arb,
                eqN = IntegerAlgebras.eqInteger,
                prA = pr,
                prN = IntegerAlgebras.printableInteger
            ))

        local("normSq is the Cayley-Dickson norm q·conj(q)") {
            lawCheckAll(arb) { q ->
                lawCheck(eq(ring.mul(q, ring.conj(q)), ring.fromBigInt(ring.normSq(q))))
            }
        }

        branch("ZModuleLipschitzQuaternion satisfies RModuleLaws") {
            suite(RModuleLaws(
                module = LipschitzQuaternionAlgebras.ZModuleLipschitzQuaternion,
                scalarArb = ArbInteger.small,
                vectorArb = arb,
                eqR = IntegerAlgebras.eqInteger,
                eqM = eq,
                prR = IntegerAlgebras.printableInteger,
                prM = pr
            ))
        }

        local("the lattice basis is the Cayley-Dickson basis 1, i, j, k and its Gram matrix is the identity") {
            val lattice = LipschitzQuaternionAlgebras.LipschitzQuaternionLattice
            lawCheck(lattice.basis == ring.basis)
            (0 until 4).forEach { r ->
                (0 until 4).forEach { c ->
                    val expected = if (r == c) BigInteger.ONE else BigInteger.ZERO
                    lawCheck(lattice.dot(lattice.basis[r], lattice.basis[c]) == expected)
                }
            }
        }

        local("the 8 Lipschitz units ±1, ±i, ±j, ±k are the invertible elements, with u⁻¹ = conj(u)") {
            ring.units.size shouldBe 8
            ring.units.forEach { u ->
                lawCheck(ring.hasReciprocal(u))
                lawCheck(eq(ring.reciprocal(u), ring.conj(u)))
                lawCheck(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
                ring.units.forEach { v -> lawCheck(ring.mul(u, v) in ring.units) }
            }
        }

        local("non-units have no reciprocal in the Lipschitz order") {
            lawCheckAll(arb) { q ->
                if (ring.normSq(q) != BigInteger.ONE) {
                    lawCheck(!ring.hasReciprocal(q))
                    shouldThrow<ArithmeticException> { ring.reciprocal(q) }
                }
            }
        }

        local("the floating-point conversion Lip → ℍ is not injective beyond 2⁵³, which is why it is not a monomorphism") {
            val big = BigInteger.TWO.pow(53)
            val q1 = quaternion(big, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
            val q2 = quaternion(big + BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
            lawCheck(!eq(q1, q2))
            lawCheck(
                QuaternionAlgebras.eqQuaternionStrict(
                    LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox(q1),
                    LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox(q2)
                )
            )
        }

        branch("LipschitzToHurwitzQuaternionMonomorphism is an injective unital ring homomorphism") {
            val mono = LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism
            suite(RingHomomorphismLaws(
                hom = mono::invoke,
                domain = ring,
                codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing,
                arb = arb,
                eqB = HurwitzQuaternionAlgebras.eqHurwitzQuaternion,
                prA = pr,
                prB = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = arb,
                eqA = eq,
                eqB = HurwitzQuaternionAlgebras.eqHurwitzQuaternion,
                prA = pr,
                prB = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
            ))
        }

        branch("LipschitzToRationalQuaternionMonomorphism is an injective unital ring homomorphism") {
            val mono = LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism
            suite(RingHomomorphismLaws(
                hom = mono::invoke,
                domain = ring,
                codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
                arb = arb,
                eqB = RationalQuaternionAlgebras.eqRationalQuaternion,
                prA = pr,
                prB = RationalQuaternionAlgebras.printableRationalQuaternion
            ))
            law(injectivityLaw(
                hom = mono::invoke,
                arbA = arb,
                eqA = eq,
                eqB = RationalQuaternionAlgebras.eqRationalQuaternion,
                prA = pr,
                prB = RationalQuaternionAlgebras.printableRationalQuaternion
            ))
        }

        local("the inclusions commute: Lip → 𝓗 → ℍ_ℚ equals Lip → ℍ_ℚ") {
            lawCheckAll(arb) { lq ->
                val viaHurwitz = HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism(
                    LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism(lq)
                )
                val direct = LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism(lq)
                lawCheck(RationalQuaternionAlgebras.eqRationalQuaternion(viaHurwitz, direct))
            }
        }
    })
})
