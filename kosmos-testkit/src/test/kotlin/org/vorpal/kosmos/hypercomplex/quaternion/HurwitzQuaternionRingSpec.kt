package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import java.math.BigInteger

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
class HurwitzQuaternionRingSpec : StringSpec({
    val ring = HurwitzQuaternionAlgebras.HurwitzQuaternionRing
    val eq = HurwitzQuaternionAlgebras.eqHurwitzQuaternion
    val pr = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
    val arb = ArbHurwitzQuaternion.hurwitzQuaternion
    val inclusion = HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism
    val ambient = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
    val eqQ = RationalQuaternionAlgebras.eqRationalQuaternion
    val prQ = RationalQuaternionAlgebras.printableRationalQuaternion

    // ── Membership ───────────────────────────────────────────────────────────

    "checked construction rejects rational quaternions outside 𝓗" {
        checkAll(ArbHurwitzQuaternion.nonHurwitzRationalQuaternion) { q ->
            shouldThrow<IllegalArgumentException> { HurwitzQuaternion(q) }
        }
    }

    "checked construction rejects mixed integer / half-integer coordinates" {
        val half = Rational.of(1, 2)
        shouldThrow<IllegalArgumentException> {
            HurwitzQuaternion(half, half, half, Rational.ONE)
        }
        shouldThrow<IllegalArgumentException> {
            HurwitzQuaternion(half, Rational.ZERO, Rational.ZERO, Rational.ZERO)
        }
    }

    "checked construction accepts both cosets and round-trips through the inclusion" {
        checkAll(arb) { h ->
            HurwitzQuaternion.isHurwitz(h.quaternion) shouldBe true
            check(eq(HurwitzQuaternion(h.quaternion), h))
            check(eqQ(inclusion(h), h.quaternion))
        }
    }

    // ── Closure and ring laws ────────────────────────────────────────────────

    "𝓗 is closed under +, -, ·, and conjugation" {
        checkAll(arb, arb) { x, y ->
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

    "HurwitzQuaternionRing satisfies InvolutiveRingLaws" {
        InvolutiveRingLaws(
            ring = ring,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }

    "normSq lands in ℤ and agrees with the norm of ℍ_ℚ" {
        checkAll(arb) { h ->
            ambient.normSq(h.quaternion) shouldBe Rational.of(ring.normSq(h), BigInteger.ONE)
        }
    }

    "ZModuleHurwitzQuaternion satisfies RModuleLaws" {
        RModuleLaws(
            module = HurwitzQuaternionAlgebras.ZModuleHurwitzQuaternion,
            scalarArb = ArbInteger.small,
            vectorArb = arb,
            eqR = IntegerAlgebras.eqInteger,
            eqM = eq,
            prR = IntegerAlgebras.printableInteger,
            prM = pr
        ).fullTest().throwIfFailed()
    }

    "the lattice basis 1, i, j, ω generates both cosets" {
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
        check(eq(k, HurwitzQuaternion.K))
    }

    // ── Units and reciprocals ────────────────────────────────────────────────

    "there are exactly 24 units, they are closed under multiplication, and u⁻¹ = conj(u)" {
        val units = ring.units
        units.size shouldBe 24
        units.toSet().size shouldBe 24
        units.forEach { u ->
            ring.hasReciprocal(u) shouldBe true
            check(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
            check(eq(ring.reciprocal(u), ring.conj(u)))
        }
        units.forEach { u -> units.forEach { v -> check(ring.mul(u, v) in units) } }
    }

    "ω = (1 + i + j + k)/2 has order 6: ω³ = -1" {
        val omega = HurwitzQuaternion.OMEGA
        val omega3 = ring.mul(ring.mul(omega, omega), omega)
        check(eq(omega3, ring.negOne))
    }

    "ρ = (-1 + i + j + k)/2 = ω - 1 is a primitive cube root of unity: ρ² + ρ + 1 = 0, so ℤ[ρ] ≅ ℤ[e^{2πi/3}] ⊂ 𝓗" {
        val rho = ring.add(HurwitzQuaternion.OMEGA, ring.negOne)
        val rhoSquared = ring.mul(rho, rho)
        check(eq(ring.add(ring.add(rhoSquared, rho), ring.one), ring.zero))
        check(eq(ring.mul(rhoSquared, rho), ring.one))
    }

    "non-units are invertible in ℍ_ℚ but not in 𝓗" {
        checkAll(arb) { h ->
            if (h != ring.zero && ring.normSq(h) != BigInteger.ONE) {
                ring.hasReciprocal(h) shouldBe false
                shouldThrow<ArithmeticException> { ring.reciprocal(h) }

                val ambientInverse = ambient.reciprocal(inclusion(h))
                check(eqQ(ambient.mul(inclusion(h), ambientInverse), ambient.one))
                HurwitzQuaternion.isHurwitz(ambientInverse) shouldBe false
            }
        }
    }

    // ── Inclusions ───────────────────────────────────────────────────────────

    "HurwitzToRationalQuaternionMonomorphism is an injective unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = inclusion::invoke,
            domain = ring,
            codomain = ambient,
            arb = arb,
            eqB = eqQ,
            prA = pr,
            prB = prQ
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = inclusion::invoke,
            arbA = arb,
            eqA = eq,
            eqB = eqQ,
            prA = pr,
            prB = prQ
        ).test()
    }
})
