package org.vorpal.kosmos.hypercomplex.bioctonion

import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.complexRingOver
import org.vorpal.kosmos.hypercomplex.octonion.Octonion
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
import org.vorpal.kosmos.hypercomplex.biquaternion.RealBiquaternion
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.biquaternionTier
import org.vorpal.kosmos.hypercomplex.biquaternion.realBiquaternionTier
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
/**
 * The bioctonion tier over [base]: the usual doubling of the biquaternions over [base], on the
 * [Bioctonion] carrier.
 */
internal fun <R : Any> bioctonionTier(
    base: CommutativeRing<R>
): OctonionTier<Complex<R>, Biquaternion<R>, Bioctonion<R>> =
    OctonionTier(
        quaternions = biquaternionTier(base),
        wrap = { cd -> Bioctonion(cd) },
        unwrap = { o -> o.cd }
    )

internal val realBioctonionTier: OctonionTier<RealComplex, RealBiquaternion, RealBioctonion> =
    OctonionTier(
        quaternions = realBiquaternionTier,
        wrap = { cd -> Bioctonion(cd) },
        unwrap = { o -> o.cd }
    )

private val cayleyDicksonBioctonion: NonAssociativeInvolutiveRing<RealBioctonion> =
    realBioctonionTier.ring

/**
 * The coefficientwise complex scalar action.
 */
private val complexScaling: LeftAction<RealComplex, RealBioctonion> =
    realBioctonionTier.scalarAction

/**
 * Algebraic structures, morphisms and utilities over the [RealBioctonion]s `𝕆 ⊗_ℝ ℂ`.
 *
 * Throughout, `e₁, …, e₇` are the octonion units and `h` is the imaginary unit of the coefficient
 * field ℂ, which lies in the center and the nucleus.
 *
 * Construction:
 * the usual Cayley-Dickson doubling of the biquaternions, transported to the [RealBioctonion] carrier.
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

    // ── Generic, over a commutative ring ─────────────────────────────────────

    /**
     * The bioctonion ring over a commutative base ring: the octonion ring with coefficients in
     * `Complex(R)`. Nonassociative and alternative. Its involution is the *octonion* conjugate,
     * which is `Complex(R)`-linear.
     */
    fun <R : Any> bioctonionRing(
        base: CommutativeRing<R>
    ): NonAssociativeInvolutiveRing<Bioctonion<R>> =
        bioctonionTier(base).ring

    /**
     * The standard `Complex(R)`-basis `1, e₁, …, e₇` over [base].
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Bioctonion<R>> =
        bioctonionTier(base).basis

    /**
     * The central imaginary scalar `h` over [base]: the imaginary unit of `Complex(R)`, `h² = -1`.
     */
    fun <R : Any> centralUnit(
        base: CommutativeRing<R>
    ): Bioctonion<R> =
        bioctonionTier(base).scalarEmbeddingMap(complex(base.zero, base.one))

    /**
     * The `Complex(R)`-valued norm `N(o) = o·conj(o) = c₀² + … + c₇²`. It is multiplicative.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Bioctonion<R>, Complex<R>> =
        bioctonionTier(base).normSq

    /**
     * The complex conjugate over [base]: conjugates each coefficient (`h ↦ -h`). An *automorphism*.
     * Its fixed ring is `Octonion(R)` when `R` has no 2-torsion; in general `a + bh` is fixed
     * exactly when `2b = 0`, so in characteristic two it is the identity.
     */
    fun <R : Any> complexConj(
        base: CommutativeRing<R>
    ): Endo<Bioctonion<R>> {
        val conj = complexRingOver(base).conj
        return Endo("${Symbols.CONJ}ℂ") { o -> bioctonion(o.coefficients().map { c -> conj(c) }) }
    }

    /**
     * The Hermitian conjugate over [base]: the complex conjugate of the octonion conjugate. An
     * involutive anti-automorphism.
     */
    fun <R : Any> hermitianConj(
        base: CommutativeRing<R>
    ): Endo<Bioctonion<R>> {
        val octonionConj = bioctonionTier(base).ring.conj
        val coefficientConj = complexConj(base)
        return Endo(Symbols.DAGGER) { o -> coefficientConj(octonionConj(o)) }
    }

    /**
     * The coefficientwise action of `Complex(R)`.
     */
    fun <R : Any> complexLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<Complex<R>, Bioctonion<R>> =
        bioctonionTier(base).scalarAction

    /**
     * The coordinatewise action of the base ring.
     */
    fun <R : Any> baseLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Bioctonion<R>> {
        val action = bioctonionTier(base).scalarAction
        return LeftAction(Symbols.TRIANGLE_RIGHT) { r, o -> action(complex(r, base.zero), o) }
    }

    /**
     * Bioctonions over a field, with reciprocals on the units.
     *
     * `o` is invertible iff `N(o)` is invertible in `Complex(F)`, and then `o⁻¹ = N(o)⁻¹ ⊳ conj(o)`,
     * a two-sided inverse despite nonassociativity because the algebra is alternative.
     */
    fun <F : Any> bioctonionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): BioctonionRingWithReciprocal<F> =
        BioctonionRingWithReciprocal(field, isZero)

    class BioctonionRingWithReciprocal<F : Any>(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean>
    ) : NonAssociativeInvolutiveRing<Bioctonion<F>> by bioctonionRing(field),
        HasNormSq<Bioctonion<F>, Complex<F>>,
        HasReciprocal<Bioctonion<F>> {

        private val complexNumbers = ComplexAlgebras.complexRingWithReciprocal(field, isZero)
        private val leftAction = complexLeftAction(field)

        override val zero: Bioctonion<F>
            get() = add.identity

        override val normSq: UnaryOp<Bioctonion<F>, Complex<F>> =
            normSq(field)

        override fun hasReciprocal(a: Bioctonion<F>): Boolean =
            complexNumbers.hasReciprocal(normSq(a))

        override val reciprocal: Endo<Bioctonion<F>> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the bioctonions")
            leftAction(complexNumbers.reciprocal(normSq(o)), conj(o))
        }
    }

    /**
     * `Bioctonion<R>` as a free `R`-module of rank 16.
     */
    fun <R : Any> bioctonionModule(
        base: CommutativeRing<R>
    ): RModule<R, Bioctonion<R>> =
        RModule.of(
            scalars = base,
            add = bioctonionRing(base).add,
            leftAction = baseLeftAction(base)
        )

    /**
     * `Bioctonion<R>` as a nonassociative star algebra over `Complex(R)` under the octonion
     * conjugate.
     */
    fun <R : Any> bioctonionStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<Complex<R>, Bioctonion<R>> =
        NonAssociativeStarAlgebra.of(
            scalars = complexRingOver(base),
            involutiveRing = bioctonionRing(base),
            leftAction = complexLeftAction(base)
        )

    /**
     * `Bioctonion<R>` as a nonassociative star algebra over `R` under the Hermitian conjugate.
     */
    fun <R : Any> bioctonionHermitianStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<R, Bioctonion<R>> {
        val ring = bioctonionRing(base)
        return NonAssociativeStarAlgebra.of(
            scalars = base,
            involutiveRing = NonAssociativeInvolutiveRing.of(ring.add, ring.mul, hermitianConj(base)),
            leftAction = baseLeftAction(base)
        )
    }

    /**
     * The embedding of `Complex(R)` as the scalars `c ↦ c·1`.
     */
    fun <R : Any> complexToBioctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Complex<R>, Bioctonion<R>> {
        val tier = bioctonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = tier.quaternions.scalars,
            codomain = tier.ring,
            map = tier.scalarEmbeddingMap
        )
    }

    /**
     * The embedding `Octonion(R) ↪ Bioctonion(R)` as the bioctonions whose coefficients have no
     * imaginary part.
     */
    fun <R : Any> octonionToBioctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Octonion<R>, Bioctonion<R>> =
        NonAssociativeRingMonomorphism.of(
            domain = OctonionAlgebras.octonionRing(base),
            codomain = bioctonionRing(base),
            map = UnaryOp { o -> bioctonion(o.coefficients().map { r -> complex(r, base.zero) }) }
        )

    /**
     * The embedding `SplitOctonion(R) ↪ Bioctonion(R)`: `e₁, e₂, e₃` are kept and
     * `e₄, …, e₇ ↦ h·e₄, …, h·e₇`.
     */
    fun <R : Any> splitOctonionToBioctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<SplitOctonion<R>, Bioctonion<R>> =
        NonAssociativeRingMonomorphism.of(
            domain = SplitOctonionAlgebras.splitOctonionRing(base),
            codomain = bioctonionRing(base),
            map = UnaryOp { o ->
                val cs = o.coefficients()
                bioctonion(
                    cs.take(4).map { r -> complex(r, base.zero) } + cs.drop(4).map { r -> complex(base.zero, r) }
                )
            }
        )

    /**
     * The canonical Cayley-Dickson embedding `Biquaternion(R) ↪ Bioctonion(R)`, `q ↦ (q, 0)`.
     */
    fun <R : Any> biquaternionToBioctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Biquaternion<R>, Bioctonion<R>> {
        val tier = bioctonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = tier.quaternions.ring,
            codomain = tier.ring,
            map = tier.canonicalEmbeddingMap
        )
    }

    /**
     * Coordinatewise equality on `Bioctonion<R>`.
     */
    fun <R : Any> eqBioctonion(base: CommutativeRing<R>, eqR: Eq<R>): Eq<Bioctonion<R>> =
        bioctonionTier(base).eq(ComplexAlgebras.eqComplex(eqR))

    // ── The complex octonions 𝕆 ⊗ ℂ ──────────────────────────────────────────

    object BioctonionAlgebra :
        NonAssociativeInvolutiveRing<RealBioctonion> by cayleyDicksonBioctonion,
        HasNormSq<RealBioctonion, RealComplex>,
        HasReciprocal<RealBioctonion> {

        private val complexField = ComplexAlgebras.ComplexField
        private val complexStar = ComplexAlgebras.ComplexStarAlgebra

        override val zero: RealBioctonion = cayleyDicksonBioctonion.zero

        /**
         * The standard ℂ-basis `1, e₁, …, e₇`.
         */
        val basis: List<RealBioctonion> =
            realBioctonionTier.basis

        val basisMap: Map<Int, RealBioctonion> =
            realBioctonionTier.basisMap

        /** The central imaginary scalar `h` (the `i` of the coefficient field ℂ), with `h² = -1`. */
        val h: RealBioctonion =
            bioctonion(listOf(ComplexAlgebras.i) + List(7) { complexField.zero })

        /**
         * The complex conjugate: conjugates each coefficient, `h ↦ -h`, fixing `e₁, …, e₇`.
         *
         * It is an *automorphism* (it preserves the order of products) and is ℂ-antilinear. Its fixed
         * ring is the real octonions 𝕆.
         */
        val complexConj: Endo<RealBioctonion> = Endo("${Symbols.CONJ}ℂ") { o ->
            bioctonion(o.coefficients().map(complexStar.conj::invoke))
        }

        /**
         * The Hermitian conjugate `o ↦ complexConj(conj(o))`: an involutive anti-automorphism that is
         * ℂ-antilinear.
         */
        val hermitianConj: Endo<RealBioctonion> = Endo(Symbols.DAGGER) { o ->
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
        override val normSq: UnaryOp<RealBioctonion, RealComplex> =
            realBioctonionTier.normSq

        /**
         * `o` is invertible iff `N(o)` is invertible in ℂ. Following the floating-point policy of
         * [HasReciprocal], that means `N(o)` is finite and not approximately zero.
         */
        override fun hasReciprocal(a: RealBioctonion): Boolean =
            complexField.hasReciprocal(normSq(a))

        /**
         * The reciprocal `o⁻¹ = N(o)⁻¹ ⊳ conj(o)`: a two-sided inverse despite nonassociativity,
         * because the algebra is alternative.
         */
        override val reciprocal: Endo<RealBioctonion> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the bioctonions")
            complexScaling(complexField.reciprocal(normSq(o)), conj(o))
        }
    }

    /**
     * The bioctonions as an eight-dimensional vector space over ℂ.
     */
    val BioctonionComplexVectorSpace: FiniteVectorSpace<RealComplex, RealBioctonion> = FiniteVectorSpace.of(
        scalars = ComplexAlgebras.ComplexField,
        add = BioctonionAlgebra.add,
        dimension = 8,
        leftAction = complexScaling
    )

    /**
     * The bioctonions as a sixteen-dimensional vector space over ℝ.
     */
    val BioctonionRealVectorSpace: FiniteVectorSpace<Real, RealBioctonion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = BioctonionAlgebra.add,
        dimension = 16,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, o -> complexScaling(complex(r, 0.0), o) }
    )

    /**
     * The nonassociative ℂ-star-algebra of bioctonions under the octonion conjugate, which is
     * ℂ-linear.
     */
    val BioctonionStarAlgebra: NonAssociativeStarAlgebra<RealComplex, RealBioctonion> = NonAssociativeStarAlgebra.of(
        scalars = ComplexAlgebras.ComplexField,
        involutiveRing = BioctonionAlgebra,
        leftAction = complexScaling
    )

    /**
     * The nonassociative ℝ-star-algebra of bioctonions under the Hermitian conjugate, which is only
     * ℝ-linear.
     */
    val BioctonionHermitianStarAlgebra: NonAssociativeStarAlgebra<Real, RealBioctonion> = NonAssociativeStarAlgebra.of(
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
    val ComplexToBioctonionMonomorphism: NonAssociativeRingMonomorphism<RealComplex, RealBioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = ComplexAlgebras.ComplexField,
            codomain = BioctonionAlgebra,
            map = realBioctonionTier.scalarEmbeddingMap
        )

    /**
     * The embedding `𝕆 ↪ 𝕆 ⊗ ℂ` of the real octonions as the bioctonions with real coefficients:
     * the fixed ring of [BioctonionAlgebra.complexConj].
     */
    val OctonionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<RealOctonion, RealBioctonion> =
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
    val SplitOctonionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<SplitOctonion<Real>, RealBioctonion> =
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
    val BiquaternionToBioctonionMonomorphism: NonAssociativeRingMonomorphism<RealBiquaternion, RealBioctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = BiquaternionAlgebras.BiquaternionRing,
            codomain = BioctonionAlgebra,
            map = realBioctonionTier.canonicalEmbeddingMap
        )

    val eqBioctonionStrict: Eq<RealBioctonion> =
        realBioctonionTier.eq(ComplexAlgebras.eqComplexStrict)

    val eqBioctonion: Eq<RealBioctonion> =
        realBioctonionTier.eq(ComplexAlgebras.eqComplex)

    /**
     * The machinery for the signed-basis-unit embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`; see [OctonionEmbeddingKit].
     */
    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<RealComplex, RealBiquaternion, RealBioctonion> =
        realBioctonionTier.embeddingKit(eqBioctonionStrict)

    /**
     * The 168 signed-basis-unit embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`: the ℂ-linear extensions of the 168
     * embeddings `ℍ ↪ 𝕆`.
     */
    fun allBiquaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RealBiquaternion, RealBioctonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     */
    fun normalizedBiquaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RealBiquaternion, RealBioctonion>> =
        embeddingKit.normalizedEmbeddings()

    private fun printableBioctonionGenerator(
        prComplex: Printable<RealComplex>,
        eqComplex: Eq<RealComplex>
    ): Printable<RealBioctonion> =
        complexCoefficientPrintable(
            labels = listOf("", "e1", "e2", "e3", "e4", "e5", "e6", "e7"),
            prComplex = prComplex,
            eqComplex = eqComplex
        ) { o: RealBioctonion -> o.coefficients() }

    val printableBioctonion: Printable<RealBioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplex, ComplexAlgebras.eqComplex)

    val printableBioctonionStrict: Printable<RealBioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplexStrict, ComplexAlgebras.eqComplexStrict)

    val printableBioctonionPretty: Printable<RealBioctonion> =
        printableBioctonionGenerator(ComplexAlgebras.printableComplexPretty, ComplexAlgebras.eqComplex)
}
