package org.vorpal.kosmos.hypercomplex.biquaternion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.checkAll
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.multi.ArbBicomplex
import org.vorpal.kosmos.hypercomplex.multi.BicomplexAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * The maps into and out of the biquaternions: the center ℂ, the two real forms ℍ and ℍ', the
 * bicomplex numbers, and the isomorphism with `M₂(ℂ)`.
 */
class BiquaternionMorphismSpec : StringSpec({
    val ring = BiquaternionAlgebras.BiquaternionRing
    val eq = BiquaternionAlgebras.eqBiquaternion
    val pr = BiquaternionAlgebras.printableBiquaternionPretty
    val arb = ArbBiquaternion.biquaternion
    val complexField = ComplexAlgebras.ComplexField
    val complexStar = ComplexAlgebras.ComplexStarAlgebra
    val eqC = ComplexAlgebras.eqComplex
    val prC = ComplexAlgebras.printableComplexPretty

    val iso = BiquaternionAlgebras.BiquaternionToComplexMatrixIsomorphism
    val matrices = DenseMatAlgebras.DenseMatRing(complexField, 2)
    val eqM = DenseMatAlgebras.liftEq(eqC)
    val prM = DenseMatAlgebras.liftPrintablePretty(prC)

    val arbMatrix: Arb<DenseMat<Complex>> =
        ArbBiquaternion.smallComplex.let { c ->
            Arb.bind(c, c, c, c) { p, q, r, s -> DenseMat.ofRows(listOf(listOf(p, q), listOf(r, s))) }
        }

    fun det(m: DenseMat<Complex>): Complex =
        complexField.add(
            complexField.mul(m[0, 0], m[1, 1]),
            complexField.add.inverse(complexField.mul(m[0, 1], m[1, 0]))
        )

    fun conjugateTranspose(m: DenseMat<Complex>): DenseMat<Complex> =
        DenseMat.ofRows(
            listOf(
                listOf(complexStar.conj(m[0, 0]), complexStar.conj(m[1, 0])),
                listOf(complexStar.conj(m[0, 1]), complexStar.conj(m[1, 1]))
            )
        )

    "ℂ ↪ ℍ ⊗ ℂ is an injective unital ring homomorphism onto the center" {
        val mono = BiquaternionAlgebras.ComplexToBiquaternionMonomorphism
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = complexField,
            codomain = ring,
            arb = ArbBiquaternion.smallComplex,
            eqB = eq,
            prA = prC,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(mono::invoke, ArbBiquaternion.smallComplex, eqC, eq, prC, pr).test()

        checkAll(ArbBiquaternion.smallComplex, arb) { c, q ->
            check(eq(ring.mul(mono(c), q), ring.mul(q, mono(c))))
        }
    }

    "ℍ ↪ ℍ ⊗ ℂ is an injective unital ring homomorphism onto the fixed ring of the complex conjugate" {
        val mono = BiquaternionAlgebras.QuaternionToBiquaternionMonomorphism
        val quaternions = QuaternionAlgebras.QuaternionDivisionRing
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = quaternions,
            codomain = ring,
            arb = ArbQuaternion.quaternion,
            eqB = eq,
            prA = QuaternionAlgebras.printableQuaternionPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbQuaternion.quaternion, QuaternionAlgebras.eqQuaternion, eq,
            QuaternionAlgebras.printableQuaternionPretty, pr
        ).test()

        quaternions.basis.map(mono::invoke) shouldBe ring.basis

        checkAll(ArbQuaternion.quaternion) { q ->
            check(eq(ring.complexConj(mono(q)), mono(q)))
            // On the real form the three conjugations collapse to two: conj and the identity.
            check(eq(ring.hermitianConj(mono(q)), mono(quaternions.conj(q))))
            check(eqC(ring.normSq(mono(q)), CDReal(quaternions.normSq(q))))
        }
    }

    "ℍ' ↪ ℍ ⊗ ℂ is an injective unital ring homomorphism: i ↦ i, j ↦ h·j, k ↦ h·k" {
        val mono = BiquaternionAlgebras.SplitQuaternionToBiquaternionMonomorphism
        val split = SplitQuaternionAlgebras.RealSplitQuaternionRing
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = split,
            codomain = ring,
            arb = ArbSplitQuaternion.smallSplitQuaternion,
            eqB = eq,
            prA = SplitQuaternionAlgebras.printableSplitQuaternionPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbSplitQuaternion.smallSplitQuaternion, SplitQuaternionAlgebras.eqSplitQuaternion, eq,
            SplitQuaternionAlgebras.printableSplitQuaternionPretty, pr
        ).test()

        check(eq(mono(SplitQuaternionAlgebras.i), ring.i))
        check(eq(mono(SplitQuaternionAlgebras.j), ring.mul(ring.h, ring.j)))
        check(eq(mono(SplitQuaternionAlgebras.k), ring.mul(ring.h, ring.k)))

        checkAll(ArbSplitQuaternion.smallSplitQuaternion) { q ->
            check(eqC(ring.normSq(mono(q)), CDReal(split.normSq(q))))
        }
    }

    "𝔹 ↪ ℍ ⊗ ℂ is an injective unital ring homomorphism: j_𝔹 ↦ i and i_𝔹 ↦ h" {
        val mono = BiquaternionAlgebras.BicomplexToBiquaternionMonomorphism
        val bicomplex = BicomplexAlgebras.BicomplexCommutativeRing
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = bicomplex,
            codomain = ring,
            arb = ArbBicomplex.bicomplex,
            eqB = eq,
            prA = BicomplexAlgebras.printableBicomplexPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            mono::invoke, ArbBicomplex.bicomplex, BicomplexAlgebras.eqBicomplex, eq,
            BicomplexAlgebras.printableBicomplexPretty, pr
        ).test()

        check(eq(mono(bicomplex.j), ring.i))
        check(eq(mono(bicomplex.i), ring.h))

        // The Cayley-Dickson conjugation of 𝔹 (conj2) is the restriction of the quaternion conjugate.
        checkAll(ArbBicomplex.bicomplex) { b ->
            check(eq(mono(bicomplex.conj2(b)), ring.conj(mono(b))))
        }
    }

    "ℍ ⊗ ℂ → M₂(ℂ) is a unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = iso::invoke,
            domain = ring,
            codomain = matrices,
            arb = arb,
            eqB = eqM,
            prA = pr,
            prB = prM
        ).fullTest().throwIfFailed()
    }

    "M₂(ℂ) → ℍ ⊗ ℂ is a unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = iso.backward::invoke,
            domain = matrices,
            codomain = ring,
            arb = arbMatrix,
            eqB = eq,
            prA = prM,
            prB = pr
        ).fullTest().throwIfFailed()
    }

    "the isomorphism ℍ ⊗ ℂ ≅ M₂(ℂ) and its inverse are mutually inverse" {
        checkAll(arb) { q ->
            check(eq(iso.backward(iso(q)), q))
        }
        checkAll(arbMatrix) { m ->
            check(eqM(iso(iso.backward(m)), m))
        }
    }

    "under ℍ ⊗ ℂ ≅ M₂(ℂ) the norm is the determinant" {
        checkAll(arb) { q ->
            check(eqC(det(iso(q)), ring.normSq(q)))
        }
    }

    "under ℍ ⊗ ℂ ≅ M₂(ℂ) the Hermitian conjugate is the conjugate transpose" {
        checkAll(arb) { q ->
            check(eqM(iso(ring.hermitianConj(q)), conjugateTranspose(iso(q))))
        }
    }

    "restricted to ℍ, the isomorphism is the classical representation ℍ ↪ M₂(ℂ)" {
        val classical = QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism
        val embed = BiquaternionAlgebras.QuaternionToBiquaternionMonomorphism
        checkAll(ArbQuaternion.quaternion) { q ->
            check(eqM(iso(embed(q)), classical(q)))
        }
    }
})

/** The real number `r` as a complex number. */
private fun CDReal(r: Double): Complex = org.vorpal.kosmos.hypercomplex.complex.complex(r, 0.0)
