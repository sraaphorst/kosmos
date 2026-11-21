package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Property tests for the Hurwitz order `𝓗 ⊂ ℍ_ℚ`.
 *
 * [HurwitzQuaternion] is modeled as an order element whose representation is a rational quaternion,
 * so these tests check three things:
 * 1. Membership: there is no route to an invalid instance, and the membership test is correct.
 * 2. Closure: the ring operations transported from `ℍ_ℚ` never leave `𝓗`, so the ring laws hold on `𝓗`.
 * 3. The inclusion `𝓗 ↪ ℍ_ℚ` is an injective ring homomorphism, and only the 24 units of `𝓗` are
 *    invertible *within* `𝓗`.
 */
class HurwitzQuaternionRingSpec : FunSpec({
    lawTests(lawGraph("Hurwitz quaternions") {
        val ring = HurwitzQuaternionAlgebras.HurwitzQuaternionRing
        val eq = HurwitzQuaternionAlgebras.eqHurwitzQuaternion
        val pr = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
        val arb = ArbHurwitzQuaternion.hurwitzQuaternion
        val inclusion = HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism
        val ambient = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val eqQ = RationalQuaternionAlgebras.eqRationalQuaternion
        val prQ = RationalQuaternionAlgebras.printableRationalQuaternion

        // ── Membership ───────────────────────────────────────────────────────────

        local("checked construction rejects rational quaternions outside 𝓗") {
            lawCheckAll(ArbHurwitzQuaternion.nonHurwitzRationalQuaternion) { q ->
                shouldThrow<IllegalArgumentException> { HurwitzQuaternion(q) }
            }
        }

        local("checked construction rejects mixed integer / half-integer coordinates") {
            val half = Rational.of(1, 2)
            shouldThrow<IllegalArgumentException> {
                HurwitzQuaternion(half, half, half, Rational.ONE)
            }
            shouldThrow<IllegalArgumentException> {
                HurwitzQuaternion(half, Rational.ZERO, Rational.ZERO, Rational.ZERO)
            }
        }

        local("checked construction accepts both cosets and round-trips through the inclusion") {
            lawCheckAll(arb) { h ->
                HurwitzQuaternion.isHurwitz(h.quaternion) shouldBe true
                lawCheck(eq(HurwitzQuaternion(h.quaternion), h))
                lawCheck(eqQ(inclusion(h), h.quaternion))
            }
        }

        // ── Closure and ring laws ────────────────────────────────────────────────

        local("𝓗 is closed under +, -, ·, and conjugation") {
            lawCheckAll(arb, arb) { x, y ->
                listOf(
                    ring.add(x, y),
                    ring.add.inverse(x),
                    ring.mul(x, y),
                    ring.conj(x)
                ).forEach { result ->
                    HurwitzQuaternion.isHurwitz(result.quaternion) shouldBe true
                }
            }
        }

        branch("HurwitzQuaternionRing satisfies InvolutiveRingLaws") {
            suite(InvolutiveRingLaws(
                ring = ring,
                arb = arb,
                eq = eq,
                pr = pr
            ))
        }

        local("normSq lands in ℤ and agrees with the norm of ℍ_ℚ") {
            lawCheckAll(arb) { h ->
                ambient.normSq(h.quaternion) shouldBe Rational.of(ring.normSq(h), BigInteger.ONE)
            }
        }

        branch("ZModuleHurwitzQuaternion satisfies RModuleLaws") {
            suite(RModuleLaws(
                module = HurwitzQuaternionAlgebras.ZModuleHurwitzQuaternion,
                scalarArb = ArbInteger.small,
                vectorArb = arb,
                eqR = IntegerAlgebras.eqInteger,
                eqM = eq,
                prR = IntegerAlgebras.printableInteger,
                prM = pr
            ))
        }

        local("the lattice basis 1, i, j, ω generates both cosets") {
            val lattice = HurwitzQuaternionAlgebras.HurwitzQuaternionLattice
            lattice.basis shouldBe listOf(HurwitzQuaternion.ONE, HurwitzQuaternion.I, HurwitzQuaternion.J, HurwitzQuaternion.OMEGA)

            // k = 2ω - 1 - i - j
            val two = BigInteger.TWO
            val k = listOf(
                HurwitzQuaternionAlgebras.ZModuleHurwitzQuaternion.leftAction(two, HurwitzQuaternion.OMEGA),
                ring.add.inverse(HurwitzQuaternion.ONE),
                ring.add.inverse(HurwitzQuaternion.I),
                ring.add.inverse(HurwitzQuaternion.J)
            ).reduce { acc, term -> ring.add(acc, term) }
            lawCheck(eq(k, HurwitzQuaternion.K))
        }

        // ── Units and reciprocals ────────────────────────────────────────────────

        local("there are exactly 24 units, they are closed under multiplication, and u⁻¹ = conj(u)") {
            val units = ring.units
            units.size shouldBe 24
            units.toSet().size shouldBe 24
            units.forEach { u ->
                ring.hasReciprocal(u) shouldBe true
                lawCheck(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
                lawCheck(eq(ring.reciprocal(u), ring.conj(u)))
            }
            units.forEach { u -> units.forEach { v -> lawCheck(ring.mul(u, v) in units) } }
        }

        local("ω = (1 + i + j + k)/2 has order 6: ω³ = -1") {
            val omega = HurwitzQuaternion.OMEGA
            val omega3 = ring.mul(ring.mul(omega, omega), omega)
            lawCheck(eq(omega3, ring.negOne))
        }

        local("ρ = (-1 + i + j + k)/2 = ω - 1 is a primitive cube root of unity: ρ² + ρ + 1 = 0, so ℤ[ρ] ≅ ℤ[e^{2πi/3}] ⊂ 𝓗") {
            val rho = ring.add(HurwitzQuaternion.OMEGA, ring.negOne)
            val rhoSquared = ring.mul(rho, rho)
            lawCheck(eq(ring.add(ring.add(rhoSquared, rho), ring.one), ring.zero))
            lawCheck(eq(ring.mul(rhoSquared, rho), ring.one))
        }

        local("non-units are invertible in ℍ_ℚ but not in 𝓗") {
            lawCheckAll(arb) { h ->
                if (h != ring.zero && ring.normSq(h) != BigInteger.ONE) {
                    ring.hasReciprocal(h) shouldBe false
                    shouldThrow<ArithmeticException> { ring.reciprocal(h) }

                    val ambientInverse = ambient.reciprocal(inclusion(h))
                    lawCheck(eqQ(ambient.mul(inclusion(h), ambientInverse), ambient.one))
                    HurwitzQuaternion.isHurwitz(ambientInverse) shouldBe false
                }
            }
        }

        // ── Inclusions ───────────────────────────────────────────────────────────

        branch("HurwitzToRationalQuaternionMonomorphism is an injective unital ring homomorphism") {
            suite(RingHomomorphismLaws(
                hom = inclusion::invoke,
                domain = ring,
                codomain = ambient,
                arb = arb,
                eqB = eqQ,
                prA = pr,
                prB = prQ
            ))
            law(injectivityLaw(
                hom = inclusion::invoke,
                arbA = arb,
                eqA = eq,
                eqB = eqQ,
                prA = pr,
                prB = prQ
            ))
        }
    })
})
