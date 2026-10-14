package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbLipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Property tests for the Gravesian octonions `CD⟨ℤ⟨i, j, k⟩⟩ ≅ ℤ⁸`.
 */
class GravesianOctonionSpec : StringSpec({
    val ring = GravesianOctonionAlgebras.GravesianOctonionRing
    val eq = GravesianOctonionAlgebras.eqGravesianOctonion
    val pr = GravesianOctonionAlgebras.printableGravesianOctonion
    val arb = ArbGravesianOctonion.gravesianOctonion

    "GravesianOctonionRing satisfies NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(
            algebra = ring,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }

    "Gravesian octonion multiplication is exactly alternative and Moufang" {
        AlternativityLaw(ring.mul.op, arb, eq, pr).test()
        MoufangLaw(ring.mul.op, arb, eq, pr).test()
    }

    "normSq is multiplicative in ℤ: N(xy) = N(x)N(y)" {
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

    "ZModuleGravesianOctonion satisfies RModuleLaws" {
        RModuleLaws(
            module = GravesianOctonionAlgebras.ZModuleGravesianOctonion,
            scalarArb = ArbInteger.small,
            vectorArb = arb,
            eqR = IntegerAlgebras.eqInteger,
            eqM = eq,
            prR = IntegerAlgebras.printableInteger,
            prM = pr
        ).fullTest().throwIfFailed()
    }

    "the Cayley-Dickson basis is the standard ℤ-basis of ℤ⁸ and its imaginary units square to -1" {
        check(ring.basis.size == 8)
        ring.basis.drop(1).forEach { e ->
            check(eq(ring.mul(e, e), ring.negOne))
            check(ring.normSq(e) == java.math.BigInteger.ONE)
        }
    }

    "the 16 Gravesian units ±1, ±e₁, …, ±e₇ are the invertible elements, with u⁻¹ = conj(u)" {
        ring.units.size shouldBe 16
        ring.units.forEach { u ->
            check(ring.hasReciprocal(u))
            check(eq(ring.reciprocal(u), ring.conj(u)))
            check(eq(ring.mul(u, ring.reciprocal(u)), ring.one))
            check(eq(ring.mul(ring.reciprocal(u), u), ring.one))
        }
    }

    "non-units have no reciprocal in the Gravesian octonions" {
        checkAll(arb) { o ->
            if (ring.normSq(o) != java.math.BigInteger.ONE) {
                check(!ring.hasReciprocal(o))
                shouldThrow<ArithmeticException> { ring.reciprocal(o) }
            }
        }
    }

    "all 168 embeddings Lip ↪ Gravesian are multiplicative" {
        val quaternions = LipschitzQuaternionAlgebras.LipschitzQuaternionRing
        checkAllQuaternionEmbeddingsMultiplicative(
            embeddings = GravesianOctonionAlgebras.allQuaternionEmbeddings(),
            quaternionRing = quaternions,
            quaternionBasis = quaternions.basis,
            octonionRing = ring,
            eqO = eq
        )
    }

    "the canonical Cayley-Dickson embedding Lip ↪ Gravesian is an injective ring homomorphism" {
        val mono = LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
            codomain = ring,
            arb = ArbLipschitzQuaternion.lipschitzQuaternion,
            eqB = eq,
            prA = LipschitzQuaternionAlgebras.printableLipschitzQuaternion,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = ArbLipschitzQuaternion.lipschitzQuaternion,
            eqA = LipschitzQuaternionAlgebras.eqLipschitzQuaternion,
            eqB = eq,
            prA = LipschitzQuaternionAlgebras.printableLipschitzQuaternion,
            prB = pr
        ).test()
    }

    "GravesianToRationalOctonionMonomorphism is an injective ring homomorphism" {
        val mono = GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra,
            arb = arb,
            eqB = RationalOctonionAlgebras.eqRationalOctonion,
            prA = pr,
            prB = RationalOctonionAlgebras.printableRationalOctonion
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = arb,
            eqA = eq,
            eqB = RationalOctonionAlgebras.eqRationalOctonion,
            prA = pr,
            prB = RationalOctonionAlgebras.printableRationalOctonion
        ).test()
    }

    "Lip → Gravesian → 𝕆_ℚ agrees with LipschitzToRationalOctonionMonomorphism" {
        checkAll(ArbLipschitzQuaternion.lipschitzQuaternion) { lq ->
            val composite = GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism(
                LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism(lq)
            )
            check(
                RationalOctonionAlgebras.eqRationalOctonion(
                    composite,
                    LipschitzQuaternionAlgebras.LipschitzToRationalOctonionMonomorphism(lq)
                )
            )
        }
    }
})
