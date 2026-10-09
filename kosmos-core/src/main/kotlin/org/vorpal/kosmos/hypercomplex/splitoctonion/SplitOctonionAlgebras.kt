package org.vorpal.kosmos.hypercomplex.splitoctonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.octonion.OctonionTier
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RealQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.quaternionTier
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras

/**
 * The split-octonion tier over [base]: the split doubling of the quaternions over [base].
 */
internal fun <R : Any> splitOctonionTier(
    base: CommutativeRing<R>
): OctonionTier<R, Quaternion<R>, SplitOctonion<R>> =
    OctonionTier(
        quaternions = quaternionTier(base),
        wrap = { cd -> SplitOctonion(cd) },
        unwrap = { o -> o.cd },
        split = true
    )

/**
 * Algebraic structures and utilities for the split-octonions.
 *
 * Main constructions:
 * - [splitOctonionRing]: the split-octonion ring over a commutative base ring (nonassociative,
 *   alternative).
 * - [splitOctonionRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [basis]: the standard basis `1, e₁, …, e₇`.
 * - [normSq]: the split norm of signature `(4, 4)`.
 * - [splitOctonionLeftAction]: the coordinatewise scalar action.
 * - [splitOctonionModule]: `SplitOctonion<R>` as a free `R`-module of rank 8.
 * - [splitOctonionAlgebra], [splitOctonionStarAlgebra]: as a nonassociative `R`-(star-)algebra.
 *
 * The arithmetic is not implemented here: it is [CayleyDickson.split] applied to the Hamilton
 * quaternions over `R`, transported to the [SplitOctonion] carrier.
 *
 * Real conveniences:
 * - [RealSplitOctonionAlgebra]: split-octonions over `Real`, with numerically guarded reciprocals.
 * - [RealSplitOctonionStarAlgebra]: the real nonassociative star algebra.
 * - [SplitOctonionRealVectorSpace]: the eight-dimensional real vector space.
 *
 * Morphisms:
 * - [scalarEmbedding]: `R ↪ SplitOctonion<R>`.
 * - [quaternionToSplitOctonionMonomorphism], [QuaternionToSplitOctonionMonomorphism]: the
 *   Cayley-Dickson embedding `q ↦ (q, 0)` of the quaternions, over any base and over ℝ.
 * - [splitQuaternionToSplitOctonionMonomorphism]: `ℍ' ↪ 𝕆'` onto the span of `1, e₁, e₄, e₅`.
 */
object SplitOctonionAlgebras {

    /**
     * The split-octonion ring over a commutative base ring.
     *
     * Writing an element as `(a, b) = a + bℓ` with `a, b` Hamilton quaternions over `R` and
     * `ℓ² = +1`, the product is the Cayley-Dickson product at `σ = -1`:
     * ```text
     * (a, b)(c, d) = (ac + d*b, da + bc*).
     * ```
     * It is alternative and flexible and satisfies the Moufang identities, but it is neither
     * associative nor commutative. The involution is the Cayley-Dickson conjugation, which negates
     * all seven imaginary coordinates.
     */
    fun <R : Any> splitOctonionRing(
        base: CommutativeRing<R>
    ): NonAssociativeInvolutiveRing<SplitOctonion<R>> =
        splitOctonionTier(base).ring

    /**
     * The standard basis `1, e₁, …, e₇` over [base]: `e₁, e₂, e₃` are `i, j, k` in the first slot
     * (squaring to `-1`), and `e₄, …, e₇` are `1, i, j, k` in the second (squaring to `+1`).
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<SplitOctonion<R>> =
        splitOctonionTier(base).basis

    /**
     * The split norm
     * ```text
     * N(o) = o·conj(o) = w² + x² + y² + z² - u² - v² - s² - t².
     * ```
     * It is multiplicative, `N(pq) = N(p)N(q)`: the split-octonions are a composition algebra.
     * `N(o) = 0` on the null cone, which consists of the zero divisors (and 0).
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<SplitOctonion<R>, R> =
        splitOctonionTier(base).normSq

    /**
     * The coordinatewise scalar action. The scalars lie in the nucleus and the center, so this is
     * also multiplication by the embedded scalar.
     */
    fun <R : Any> splitOctonionLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, SplitOctonion<R>> =
        splitOctonionTier(base).scalarAction

    /**
     * Split-octonions over a field, with reciprocals on the units.
     *
     * `o` is invertible iff `N(o)` is invertible in [field], and then
     * ```text
     * o⁻¹ = conj(o) / N(o),
     * ```
     * a two-sided inverse despite nonassociativity, because the algebra is alternative.
     * [isZero] decides when a norm counts as zero; over `Real` a numerical test should be supplied
     * (see [RealSplitOctonionAlgebra]).
     */
    fun <F : Any> splitOctonionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): SplitOctonionRingWithReciprocal<F> =
        SplitOctonionRingWithReciprocal(field, isZero)

    class SplitOctonionRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : NonAssociativeInvolutiveRing<SplitOctonion<F>> by splitOctonionRing(field),
        HasNormSq<SplitOctonion<F>, F>,
        HasReciprocal<SplitOctonion<F>> {

        private val leftAction = splitOctonionLeftAction(field)

        override val zero: SplitOctonion<F>
            get() = add.identity

        override val normSq: UnaryOp<SplitOctonion<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: SplitOctonion<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<SplitOctonion<F>> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the split-octonions")
            leftAction(field.reciprocal(normSq(o)), conj(o))
        }
    }

    /**
     * `SplitOctonion<R>` as a free `R`-module of rank 8.
     */
    fun <R : Any> splitOctonionModule(
        base: CommutativeRing<R>
    ): RModule<R, SplitOctonion<R>> =
        RModule.of(
            scalars = base,
            add = splitOctonionRing(base).add,
            leftAction = splitOctonionLeftAction(base)
        )

    /**
     * `SplitOctonion<R>` as a nonassociative `R`-algebra.
     */
    fun <R : Any> splitOctonionAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeAlgebra<R, SplitOctonion<R>> =
        NonAssociativeAlgebra.of(
            scalars = base,
            algebraRing = splitOctonionRing(base),
            leftAction = splitOctonionLeftAction(base)
        )

    /**
     * `SplitOctonion<R>` as a nonassociative `R`-star-algebra under the Cayley-Dickson conjugation.
     */
    fun <R : Any> splitOctonionStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<R, SplitOctonion<R>> =
        NonAssociativeStarAlgebra.of(
            scalars = base,
            involutiveRing = splitOctonionRing(base),
            leftAction = splitOctonionLeftAction(base)
        )

    /**
     * The split-octonions over the reals, `𝕆'`.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `o` is invertible iff `N(o)`
     * is finite and not approximately zero.
     */
    object RealSplitOctonionAlgebra :
        NonAssociativeInvolutiveRing<SplitOctonion<Real>> by splitOctonionRing(RealAlgebras.RealField),
        HasNormSq<SplitOctonion<Real>, Real>,
        HasReciprocal<SplitOctonion<Real>> {

        private val guarded = splitOctonionRingWithReciprocal(
            field = RealAlgebras.RealField,
            isZero = UnaryOp { n -> !n.isFinite() || RealAlgebras.eqRealApprox(n, 0.0) }
        )

        override val zero: SplitOctonion<Real>
            get() = add.identity

        override val normSq: UnaryOp<SplitOctonion<Real>, Real> = guarded.normSq

        override fun hasReciprocal(a: SplitOctonion<Real>): Boolean =
            guarded.hasReciprocal(a)

        override val reciprocal: Endo<SplitOctonion<Real>> = guarded.reciprocal

        /**
         * The standard basis `1, e₁, …, e₇`.
         */
        val basis: List<SplitOctonion<Real>> =
            basis(RealAlgebras.RealField)
    }

    /**
     * The split-octonion nonassociative star algebra over the reals.
     */
    object RealSplitOctonionStarAlgebra :
        NonAssociativeInvolutiveRing<SplitOctonion<Real>> by RealSplitOctonionAlgebra,
        NonAssociativeStarAlgebra<Real, SplitOctonion<Real>> {

        override val zero: SplitOctonion<Real> = RealSplitOctonionAlgebra.zero
        override val one: SplitOctonion<Real> = RealSplitOctonionAlgebra.one

        override val scalars = RealAlgebras.RealField

        override val leftAction: LeftAction<Real, SplitOctonion<Real>> =
            splitOctonionLeftAction(RealAlgebras.RealField)
    }

    /**
     * `𝕆'` as an eight-dimensional real vector space; the scalars act coordinatewise.
     */
    object SplitOctonionRealVectorSpace : FiniteVectorSpace<Real, SplitOctonion<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealSplitOctonionAlgebra.add
        override val dimension = 8
        override val leftAction: LeftAction<Real, SplitOctonion<Real>> =
            RealSplitOctonionStarAlgebra.leftAction
    }

    /**
     * The embedding of the base ring as the scalars.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<R, SplitOctonion<R>> {
        val tier = splitOctonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = base,
            codomain = tier.ring,
            map = tier.scalarEmbeddingMap
        )
    }

    /**
     * The Cayley-Dickson embedding `ℍ ↪ 𝕆'`, `q ↦ (q, 0)`: the quaternions are the span of
     * `1, e₁, e₂, e₃`, the positive-definite half of the split norm.
     */
    object QuaternionToSplitOctonionMonomorphism : NonAssociativeRingMonomorphism<RealQuaternion, SplitOctonion<Real>> {
        override val domain = QuaternionAlgebras.QuaternionDivisionRing
        override val codomain = RealSplitOctonionAlgebra
        override val map: UnaryOp<RealQuaternion, SplitOctonion<Real>> =
            quaternionToSplitOctonionMonomorphism(RealAlgebras.RealField).map
    }

    /**
     * The Cayley-Dickson embedding `Quaternion<R> ↪ SplitOctonion<R>`, `q ↦ (q, 0)`, over any base.
     */
    fun <R : Any> quaternionToSplitOctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Quaternion<R>, SplitOctonion<R>> {
        val tier = splitOctonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = tier.quaternions.ring,
            codomain = tier.ring,
            map = tier.canonicalEmbeddingMap
        )
    }

    /**
     * The embedding `ℍ' ↪ 𝕆'` of the split-quaternions onto the span of `1, e₁, e₄, e₅`:
     * ```text
     * i ↦ e₁,   j ↦ e₄,   k ↦ e₅.
     * ```
     * Writing a split-quaternion as `(a, b)` with `a, b ∈ R[i]`, this is `(a, b) ↦ ((a, 0), (b, 0))`:
     * the split doubling of the complex-like line inside the split doubling of the quaternions.
     */
    fun <R : Any> splitQuaternionToSplitOctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<SplitQuaternion<R>, SplitOctonion<R>> {
        val zero = CD(base.zero, base.zero)
        return NonAssociativeRingMonomorphism.of(
            domain = SplitQuaternionAlgebras.splitQuaternionRing(base),
            codomain = splitOctonionRing(base),
            map = UnaryOp { q -> SplitOctonion(CD(CD(q.cd.a, zero), CD(q.cd.b, zero))) }
        )
    }

    /**
     * Eqs
     */
    fun <R : Any> eqSplitOctonion(eqR: Eq<R>): Eq<SplitOctonion<R>> =
        CD.eq(CD.eq(CD.eq(eqR))).contramap { o: SplitOctonion<R> -> o.cd }

    /**
     * Strict equality on real split-octonions.
     */
    val eqSplitOctonionStrict: Eq<SplitOctonion<Real>> =
        eqSplitOctonion(RealAlgebras.eqRealStrict)

    /**
     * Approximate equality on real split-octonions.
     */
    val eqSplitOctonion: Eq<SplitOctonion<Real>> =
        eqSplitOctonion(RealAlgebras.eqRealApprox)

    /**
     * Printables
     */
    fun <R : Any> printableSplitOctonion(
        base: CommutativeRing<R>,
        signed: SignedOps<R>,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<SplitOctonion<R>> =
        splitOctonionTier(base).printable(signed, prR, eqR)

    private fun printableSplitOctonionRealGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<SplitOctonion<Real>> =
        printableSplitOctonion(RealAlgebras.RealField, RealAlgebras.SignedReal, prReal, eqReal)

    val printableSplitOctonionReal: Printable<SplitOctonion<Real>> =
        printableSplitOctonionRealGenerator(RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableSplitOctonionStrict: Printable<SplitOctonion<Real>> =
        printableSplitOctonionRealGenerator(RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableSplitOctonionPretty: Printable<SplitOctonion<Real>> =
        printableSplitOctonionRealGenerator(RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
