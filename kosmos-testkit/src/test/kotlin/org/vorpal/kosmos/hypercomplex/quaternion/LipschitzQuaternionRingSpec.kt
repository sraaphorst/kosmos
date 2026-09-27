package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import java.math.BigInteger

/**
 * Property tests for the Lipschitz quaternions `ℤ⟨i, j, k⟩ = CD⟨ℤ[i]⟩` and their inclusions
 * `ℤ⟨i, j, k⟩ ⊂ 𝓗 ⊂ ℍ_ℚ → ℍ`.
 */
class LipschitzQuaternionRingSpec : StringSpec({
    val ring = LipschitzQuaternionAlgebras.LipschitzQuaternionRing
    val eq = LipschitzQuaternionAlgebras.eqLipschitzQuaternion
    val pr = LipschitzQuaternionAlgebras.printableLipschitzQuaternion
    val arb = ArbLipschitzQuaternion.lipschitzQuaternion

    "LipschitzQuaternionRing satisfies InvolutiveRingLaws" {
        InvolutiveRingLaws(
            ring = ring,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }

    "normSq is multiplicative: N(xy) = N(x)N(y) in ℤ" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = ring.normSq,
            mulN = IntegerAlgebras.IntegerCommutativeRing.mul.op,
            arbA = arb,
            eqN = IntegerAlgebras.eqInteger,
            prA = pr,
            prN = IntegerAlgebras.printableInteger
        ).test()
    }

    "normSq is the Cayley-Dickson norm q·conj(q)" {
        checkAll(arb) { q ->
            check(eq(ring.mul(q, ring.conj(q)), ring.fromBigInt(ring.normSq(q))))
        }
    }

    "ZModuleLipschitzQuaternion satisfies RModuleLaws" {
        RModuleLaws(
            module = LipschitzQuaternionAlgebras.ZModuleLipschitzQuaternion,
            scalarArb = ArbInteger.small,
            vectorArb = arb,
            eqR = IntegerAlgebras.eqInteger,
            eqM = eq,
            prR = IntegerAlgebras.printableInteger,
            prM = pr
        ).fullTest().throwIfFailed()
    }

    "the lattice basis is the Cayley-Dickson basis 1, i, j, k and its Gram matrix is the identity" {
        val lattice = LipschitzQuaternionAlgebras.LipschitzQuaternionLattice
        check(lattice.basis == ring.basis)
        (0 until 4).forEach { r ->
            (0 until 4).forEach { c ->
                val expected = if (r == c) BigInteger.ONE else BigInteger.ZERO
                check(lattice.dot(lattice.basis[r], lattice.basis[c]) == expected)
            }
        }
    }

    "the 8 Lipschitz units ±1, ±i, ±j, ±k are the invertible elements, with u⁻¹ = conj(u)" {
        ring.units.size shouldBe 8
        ring.units.forEach { u ->
            check(ring.hasReciprocal(u))
            check(eq(ring.reciprocal(u), ring.conj(u)))
            check(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
            ring.units.forEach { v -> check(ring.mul(u, v) in ring.units) }
        }
    }

    "non-units have no reciprocal in the Lipschitz order" {
        checkAll(arb) { q ->
            if (ring.normSq(q) != BigInteger.ONE) {
                check(!ring.hasReciprocal(q))
                shouldThrow<ArithmeticException> { ring.reciprocal(q) }
            }
        }
    }

    "the floating-point conversion Lip → ℍ is not injective beyond 2⁵³, which is why it is not a monomorphism" {
        val big = BigInteger.TWO.pow(53)
        val q1 = lipschitzQuaternion(big, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
        val q2 = lipschitzQuaternion(big + BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
        check(!eq(q1, q2))
        check(
            QuaternionAlgebras.eqQuaternionStrict(
                LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox(q1),
                LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox(q2)
            )
        )
    }

    "LipschitzToHurwitzQuaternionMonomorphism is an injective unital ring homomorphism" {
        val mono = LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing,
            arb = arb,
            eqB = HurwitzQuaternionAlgebras.eqHurwitzQuaternion,
            prA = pr,
            prB = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = arb,
            eqA = eq,
            eqB = HurwitzQuaternionAlgebras.eqHurwitzQuaternion,
            prA = pr,
            prB = HurwitzQuaternionAlgebras.printableHurwitzQuaternion
        ).test()
    }

    "LipschitzToRationalQuaternionMonomorphism is an injective unital ring homomorphism" {
        val mono = LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            arb = arb,
            eqB = RationalQuaternionAlgebras.eqRationalQuaternion,
            prA = pr,
            prB = RationalQuaternionAlgebras.printableRationalQuaternion
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = arb,
            eqA = eq,
            eqB = RationalQuaternionAlgebras.eqRationalQuaternion,
            prA = pr,
            prB = RationalQuaternionAlgebras.printableRationalQuaternion
        ).test()
    }

    "the inclusions commute: Lip → 𝓗 → ℍ_ℚ equals Lip → ℍ_ℚ" {
        checkAll(arb) { lq ->
            val viaHurwitz = HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism(
                LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism(lq)
            )
            val direct = LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism(lq)
            check(RationalQuaternionAlgebras.eqRationalQuaternion(viaHurwitz, direct))
        }
    }
})
