package org.vorpal.kosmos.hypercomplex.bioctonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.biquaternion.Biquaternion
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.biquaternionTier
import org.vorpal.kosmos.hypercomplex.biquaternion.complexCoefficientPrintable
import org.vorpal.kosmos.hypercomplex.complex.RealComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.octonion.RealOctonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.OctonionEmbeddingKit
import org.vorpal.kosmos.hypercomplex.octonion.OctonionTier
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras

/**
 * `𝕆 ⊗ ℂ = CD⟨ℍ ⊗ ℂ⟩`: the octonion tier with complex coefficients.
 *
 * File-level so that initializing [BioctonionAlgebras.BioctonionAlgebra] never recursively
 * initializes [BioctonionAlgebras].
 */
internal val bioctonionTier: OctonionTier<RealComplex, Biquaternion, Bioctonion> =
    OctonionTier(
        quaternions = biquaternionTier,
        wrap = { cd -> Bioctonion(cd) },
        unwrap = { o -> o.cd }
    )

private val cayleyDicksonBioctonion: NonAssociativeInvolutiveRing<Bioctonion> =
    bioctonionTier.ring

/**
 * The coefficientwise complex scalar action.
 */
private val complexScaling: LeftAction<RealComplex, Bioctonion> =
    bioctonionTier.scalarAction

/**
 * Algebraic structures, morphisms and utilities over the [Bioctonion]s `𝕆 ⊗_ℝ ℂ`.
 *
 * Throughout, `e₁, …, e₇` are the octonion units and `h` is the imaginary unit of the coefficient
 * field ℂ, which lies in the center and the nucleus.
 *
 * Construction:
 * the usual Cayley-Dickson doubling of the biquaternions, transported to the [Bioctonion] carrier.
 * Nothing in this file re-implements a product.
 *
 * Algebra and its units:
 * - [BioctonionAlgebra]: the nonassociative, alternative ring. It exposes [HasNormSq] (the
 *   complex-valued norm) and [HasReciprocal] (the units are the elements of nonzero norm).
 * - [BioctonionAlgebra.basis], [BioctonionAlgebra.h].
 *
 * The three conjugations (as for the biquaternions):
 * - [BioctonionAlgebra.conj]: the *octonion* conjugate, negating `e₁, …, e₇`. ℂ-linear.
 * - [BioctonionAlgebra.complexConj]: the *complex* conjugate of each coefficient. An automorphism.
 * - [BioctonionAlgebra.hermitianConj]: the composite. ℂ-antilinear; an anti-automorphism.
 *
 * Vector spaces and star algebras:
 * - [BioctonionComplexVectorSpace] (dimension 8), [BioctonionRealVectorSpace] (dimension 16).
 * - [BioctonionStarAlgebra]: the nonassociative ℂ-star-algebra for the octonion conjugate.
 * - [BioctonionHermitianStarAlgebra]: the nonassociative ℝ-star-algebra for the Hermitian conjugate.
 *
 * Homomorphisms and embeddings:
 * - [ComplexToBioctonionMonomorphism]: `ℂ ↪ 𝕆 ⊗ ℂ` as the scalars.
 * - [OctonionToBioctonionMonomorphism]: `𝕆 ↪ 𝕆 ⊗ ℂ`, the real form with real coefficients.
 * - [SplitOctonionToBioctonionMonomorphism]: `𝕆' ↪ 𝕆 ⊗ ℂ`, the other real form.
 * - [BiquaternionToBioctonionMonomorphism]: the Cayley-Dickson embedding `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`.
 * - [allBiquaternionEmbeddings], [normalizedBiquaternionEmbeddings]: the 168 (84) signed-basis-unit
 *   embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`, the ℂ-linear extensions of the embeddings `ℍ ↪ 𝕆`.
 */
object BioctonionAlgebras {

    object BioctonionAlgebra :
        NonAssociativeInvolutiveRing<Bioctonion> by cayleyDicksonBioctonion,
        HasNormSq<Bioctonion, RealComplex>,
        HasReciprocal<Bioctonion> {

        private val complexField = ComplexAlgebras.ComplexField
        private val complexStar = ComplexAlgebras.ComplexStarAlgebra

        override val zero: Bioctonion = cayleyDicksonBioctonion.zero

        /**
         * The standard ℂ-basis `1, e₁, …, e₇`.
         */
        val basis: List<Bioctonion> =
            bioctonionTier.basis

        val basisMap: Map<Int, Bioctonion> =
            bioctonionTier.basisMap

        /** The central imaginary scalar `h` (the `i` of the coefficient field ℂ), with `h² = -1`. */
        val h: Bioctonion =
            bioctonion(listOf(ComplexAlgebras.i) + List(7) { complexField.zero })

        /**
         * The complex conjugate: conjugates each coefficient, `h ↦ -h`, fixing `e₁, …, e₇`.
         *
         * It is an *automorphism* (it preserves the order of products) and is ℂ-antilinear. Its fixed
         * ring is the real octonions 𝕆.
         */
        val complexConj: Endo<Bioctonion> = Endo("${Symbols.CONJ}ℂ") { o ->
            bioctonion(o.coefficients().map(complexStar.conj::invoke))
        }

        /**
         * The Hermitian conjugate `o ↦ complexConj(conj(o))`: an involutive anti-automorphism that is
         * ℂ-antilinear.
         */
        val hermitianConj: Endo<Bioctonion> = Endo(Symbols.DAGGER) { o ->
            complexConj(conj(o))
        }

        /**
         * The complex-valued norm
         * ```text
         * N(o) = o·conj(o) = c₀² + c₁² + … + c₇² ∈ ℂ,
         * ```
         * with no conjugation of the coefficients. It is multiplicative: the bioctonions are the
         * octonion (composition) algebra over ℂ.
         */
        override val normSq: UnaryOp<Bioctonion, RealComplex> =
            bioctonionTier.normSq

        /**
         * `o` is invertible iff `N(o)` is invertible in ℂ. Following the floating-point policy of
         * [HasReciprocal], that means `N(o)` is finite and not approximately zero.
         */
        override fun hasReciprocal(a: Bioctonion): Boolean =
            complexField.hasReciprocal(normSq(a))

        /**
         * The reciprocal `o⁻¹ = N(o)⁻¹ ⊳ conj(o)`: a two-sided inverse despite nonassociativity,
         * because the algebra is alternative.
         */
        override val reciprocal: Endo<Bioctonion> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the bioctonions")
            complexScaling(complexField.reciprocal(normSq(o)), conj(o))
        }
    }

    /**
     * The bioctonions as an eight-dimensional vector space over ℂ.
     */
    val BioctonionComplexVectorSpace: FiniteVectorSpace<RealComplex, Bioctonion> = FiniteVectorSpace.of(
        scalars = ComplexAlgebras.ComplexField,
        add = BioctonionAlgebra.add,
        dimension = 8,
        leftAction = complexScaling
    )

    /**
     * The bioctonions as a sixteen-dimensional vector space over ℝ.
     */
    val BioctonionRealVectorSpace: FiniteVectorSpace<Real, Bioctonion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = BioctonionAlgebra.add,
        dimension = 16,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, o -> complexScaling(complex(r, 0.0), o) }
    )

    /**
     * The nonassociative ℂ-star-algebra of bioctonions under the octonion conjugate, which is
     * ℂ-linear.
     */
    val BioctonionStarAlgebra: NonAssociativeStarAlgebra<RealComplex, Bioctonion> = NonAssociativeStarAlgebra.of(
        scalars = ComplexAlgebras.ComplexField,
        involutiveRing = BioctonionAlgebra,
        leftAction = complexScaling
    )

    /**
     * The nonassociative ℝ-star-algebra of bioctonions under the Hermitian conjugate, which is only
     * ℝ-linear.
     */
    val BioctonionHermitianStarAlgebra: NonAssociativeStarAlgebra<Real, Bioctonion> = NonAssociativeStarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = NonAssociativeInvolutiveRing.of(
            add = BioctonionAlgebra.add,
            mul = BioctonionAlgebra.mul,
            conj = BioctonionAlgebra.hermitianConj
        ),
        leftAction = BioctonionRealVectorSpace.leftAction
    )

    /**
     * The embedding of ℂ as the scalars `c ↦ c·1`.
     */
    val ComplexToBioctonionMonomorphism: NonAssociativeRingMonomorphism<RealComplex, Bioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = ComplexAlgebras.ComplexField,
            codomain = BioctonionAlgebra,
            map = bioctonionTier.scalarEmbeddingMap
        )

    /**
     * The embedding `𝕆 ↪ 𝕆 ⊗ ℂ` of the real octonions as the bioctonions with real coefficients:
     * the fixed ring of [BioctonionAlgebra.complexConj].
     */
    val OctonionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<RealOctonion, Bioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = OctonionAlgebras.OctonionDivisionAlgebra,
            codomain = BioctonionAlgebra,
            map = UnaryOp { o -> bioctonion(o.coefficients().map { r -> complex(r, 0.0) }) }
        )

    /**
     * The embedding `𝕆' ↪ 𝕆 ⊗ ℂ` of the real split-octonions:
     * ```text
     * e₁, e₂, e₃ ↦ e₁, e₂, e₃,   e₄, …, e₇ ↦ h·e₄, …, h·e₇,
     * ```
     * i.e. `(a, b) ↦ (a, h·b)`, which turns the usual doubling into the split one because
     * `h² = -1`. The octonions and the split-octonions are the two real forms of the bioctonions.
     */
    val SplitOctonionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<SplitOctonion<Real>, Bioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
            codomain = BioctonionAlgebra,
            map = UnaryOp { o ->
                val cs = o.coefficients()
                bioctonion(cs.take(4).map { r -> complex(r, 0.0) } + cs.drop(4).map { r -> complex(0.0, r) })
            }
        )

    /**
     * The canonical Cayley-Dickson embedding `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`, `q ↦ (q, 0)`.
     */
    val BiquaternionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<Biquaternion, Bioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = BiquaternionAlgebras.BiquaternionRing,
            codomain = BioctonionAlgebra,
            map = bioctonionTier.canonicalEmbeddingMap
        )

    val eqBioctonionStrict: Eq<Bioctonion> =
        bioctonionTier.eq(ComplexAlgebras.eqComplexStrict)

    val eqBioctonion: Eq<Bioctonion> =
        bioctonionTier.eq(ComplexAlgebras.eqComplex)

    /**
     * The machinery for the signed-basis-unit embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`; see [OctonionEmbeddingKit].
     */
    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<RealComplex, Biquaternion, Bioctonion> =
        bioctonionTier.embeddingKit(eqBioctonionStrict)

    /**
     * The 168 signed-basis-unit embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`: the ℂ-linear extensions of the 168
     * embeddings `ℍ ↪ 𝕆`.
     */
    fun allBiquaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Biquaternion, Bioctonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     */
    fun normalizedBiquaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Biquaternion, Bioctonion>> =
        embeddingKit.normalizedEmbeddings()

    private fun printableBioctonionGenerator(
        prComplex: Printable<RealComplex>,
        eqComplex: Eq<RealComplex>
    ): Printable<Bioctonion> =
        complexCoefficientPrintable(
            labels = listOf("", "e1", "e2", "e3", "e4", "e5", "e6", "e7"),
            prComplex = prComplex,
            eqComplex = eqComplex
        ) { o: Bioctonion -> o.coefficients() }

    val printableBioctonion: Printable<Bioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplex, ComplexAlgebras.eqComplex)

    val printableBioctonionStrict: Printable<Bioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplexStrict, ComplexAlgebras.eqComplexStrict)

    val printableBioctonionPretty: Printable<Bioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplexPretty, ComplexAlgebras.eqComplex)
}
