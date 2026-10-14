package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
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

/**
 * The real split-quaternions `ℍ'`, and the embeddings into the split-quaternions.
 *
 * The exact algebraic facts are in [SplitQuaternionRingSpec]; here the floating-point structures are
 * checked up to approximate equality.
 */
class RealSplitQuaternionSpec : StringSpec({
    val ring = SplitQuaternionAlgebras.RealSplitQuaternionRing
    val eq = SplitQuaternionAlgebras.eqSplitQuaternion
    val pr = SplitQuaternionAlgebras.printableSplitQuaternionPretty
    val arb = ArbSplitQuaternion.smallSplitQuaternion

    "RealSplitQuaternionStarAlgebra satisfies StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = SplitQuaternionAlgebras.RealSplitQuaternionStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = arb,
            eqR = RealAlgebras.eqRealApprox,
            eqA = eq,
            prR = RealAlgebras.printableRealPretty,
            prA = pr
        ).fullTest().throwIfFailed()
    }

    "SplitQuaternionRealVectorSpace satisfies VectorSpaceLaws and has dimension 4" {
        SplitQuaternionAlgebras.SplitQuaternionRealVectorSpace.dimension shouldBe 4
        VectorSpaceLaws(
            space = SplitQuaternionAlgebras.SplitQuaternionRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = arb,
            eqF = RealAlgebras.eqRealApprox,
            eqV = eq,
            prF = RealAlgebras.printableRealPretty,
            prV = pr
        ).test().throwIfFailed()
    }

    "the real units satisfy i² = -1, j² = k² = +1, ij = k" {
        val (i, j, k) = Triple(SplitQuaternionAlgebras.i, SplitQuaternionAlgebras.j, SplitQuaternionAlgebras.k)
        check(eq(ring.mul(i, i), ring.negOne))
        check(eq(ring.mul(j, j), ring.one))
        check(eq(ring.mul(k, k), ring.one))
        check(eq(ring.mul(i, j), k))
        ring.basis shouldBe listOf(ring.one, i, j, k)
    }

    "reciprocal: q·q⁻¹ = q⁻¹·q = 1 away from the null cone" {
        checkAll(ArbSplitQuaternion.unitSplitQuaternion) { q ->
            val inverse = ring.reciprocal(q)
            check(eq(ring.mul(q, inverse), ring.one))
            check(eq(ring.mul(inverse, q), ring.one))
        }
    }

    "ℂ ↪ ℍ' is an injective unital ring homomorphism onto the plane {w + xi}" {
        val mono = SplitQuaternionAlgebras.ComplexToSplitQuaternionMonomorphism
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ComplexAlgebras.ComplexField,
            codomain = ring,
            arb = ArbComplex.boundedComplex,
            eqB = eq,
            prA = ComplexAlgebras.printableComplexPretty,
            prB = pr
        ).fullTest().throwIfFailed()

        injectivityLaw(
            hom = mono::invoke,
            arbA = ArbComplex.boundedComplex,
            eqA = ComplexAlgebras.eqComplex,
            eqB = eq,
            prA = ComplexAlgebras.printableComplexPretty,
            prB = pr
        ).test()

        checkAll(ArbComplex.boundedComplex) { c ->
            mono(c) shouldBe splitQuaternion(c.re, c.im, 0.0, 0.0)
        }
    }

    "the split-complex numbers embed along j and along k (exactly, over ℚ)" {
        val q = RationalAlgebras.RationalField
        val arbSplitComplex = ArbSplitComplex.splitComplexOf(ArbSplitQuaternion.smallRational)
        listOf(
            SplitQuaternionAlgebras.splitComplexToSplitQuaternionMonomorphism(q),
            SplitQuaternionAlgebras.splitComplexToSplitQuaternionAlongK(q)
        ).forEach { mono ->
            RingHomomorphismLaws(
                hom = mono::invoke,
                domain = SplitComplexAlgebras.splitComplexRing(q),
                codomain = SplitQuaternionAlgebras.splitQuaternionRing(q),
                arb = arbSplitComplex
            ).fullTest().throwIfFailed()

            injectivityLaw(hom = mono::invoke, arbA = arbSplitComplex).test()
        }
    }

    "the scalar embedding ℚ ↪ SplitQuaternion<ℚ> is an injective unital ring homomorphism" {
        val q = RationalAlgebras.RationalField
        val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
        RingHomomorphismLaws(
            hom = embed::invoke,
            domain = q,
            codomain = SplitQuaternionAlgebras.splitQuaternionRing(q),
            arb = ArbSplitQuaternion.smallRational
        ).fullTest().throwIfFailed()

        injectivityLaw(hom = embed::invoke, arbA = ArbSplitQuaternion.smallRational).test()
    }
})
