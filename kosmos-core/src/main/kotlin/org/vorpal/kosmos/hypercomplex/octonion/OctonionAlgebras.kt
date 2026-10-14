package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RealQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.quaternionTier

/**
 * The octonion tier over [base]: the usual doubling of the quaternions over [base], on the
 * [Octonion] carrier.
 */
internal fun <R : Any> octonionTier(
    base: CommutativeRing<R>
): OctonionTier<R, Quaternion<R>, Octonion<R>> =
    OctonionTier(
        quaternions = quaternionTier(base),
        wrap = { cd -> Octonion(cd) },
        unwrap = { o -> o.cd }
    )

/**
 * `𝕆 = Octonion<Real>`: the real octonion tier.
 *
 * File-level so that initializing [OctonionAlgebras.OctonionDivisionAlgebra] never recursively
 * initializes [OctonionAlgebras].
 */
internal val realOctonionTier: OctonionTier<Real, RealQuaternion, RealOctonion> =
    octonionTier(RealAlgebras.RealField)

/**
 * Algebraic structures and utilities for the octonions.
 *
 * Generic constructions, over any commutative base ring `R`:
 * - [octonionRing]: the octonion ring over `R` (nonassociative, alternative).
 * - [octonionRingWithReciprocal]: the same over a field, with reciprocals where the norm is
 *   invertible.
 * - [basis]: the standard basis `1, e₁, …, e₇`.
 * - [normSq]: the norm, the sum of the eight squared coordinates.
 * - [octonionLeftAction]: the coordinatewise scalar action.
 * - [octonionModule]: `Octonion<R>` as a free `R`-module of rank 8.
 * - [octonionAlgebra], [octonionStarAlgebra]: as a nonassociative `R`-(star-)algebra.
 * - [scalarEmbedding]: `R ↪ Octonion<R>`.
 * - [quaternionToOctonionMonomorphism]: the canonical Cayley-Dickson embedding `q ↦ (q, 0)`.
 * - [eqOctonion], [printableOctonion] (the overloads taking a base).
 *
 * The arithmetic is not implemented here: it is
 * [org.vorpal.kosmos.algebra.structures.CayleyDickson.usual] applied to the quaternions over `R`,
 * transported to the [Octonion] carrier.
 *
 * The real octonions 𝕆:
 * - [OctonionDivisionAlgebra]: the real octonion non-associative normed division algebra.
 * - [OctonionVectorSpace]: the eight-dimensional vector space of octonions over the reals.
 * - [OctonionStarAlgebra]: the non-associative star algebra of octonions over the reals.
 * - [QuaternionToOctonionMonomorphism]: canonical CD embedding of quaternions into octonions.
 * - [asOctonion]: convenience extension for the canonical embedding.
 * - [allQuaternionEmbeddings]: the 168 signed-basis-unit embeddings ℍ ↪ 𝕆.
 * - [eqOctonionStrict]: strict equality on real octonions.
 * - [eqOctonion]: approximate equality on real octonions.
 *
 * The rational octonions and the Gravesian integers have their own objects:
 * [RationalOctonionAlgebras], [GravesianOctonionAlgebras].
 */
object OctonionAlgebras {

    // ── Generic, over a commutative ring ─────────────────────────────────────

    /**
     * The octonion ring over a commutative base ring.
     *
     * Writing an element as `(a, b) = a + bℓ` with `a, b` quaternions over `R` and `ℓ² = -1`, the
     * product is the Cayley-Dickson product at `σ = 1`:
     * ```text
     * (a, b)(c, d) = (ac - d*b, da + bc*).
     * ```
     * It is alternative and flexible and satisfies the Moufang identities, but it is neither
     * associative nor commutative. The involution negates all seven imaginary coordinates.
     */
    fun <R : Any> octonionRing(
        base: CommutativeRing<R>
    ): NonAssociativeInvolutiveRing<Octonion<R>> =
        octonionTier(base).ring

    /**
     * The standard basis `1, e₁, …, e₇` over [base].
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Octonion<R>> =
        octonionTier(base).basis

    /**
     * The norm `N(o) = o·conj(o)`, the sum of the eight squared coordinates. It is multiplicative:
     * the octonions are a composition algebra.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Octonion<R>, R> =
        octonionTier(base).normSq

    /**
     * The coordinatewise scalar action. The scalars lie in the nucleus and the center, so this is
     * also multiplication by the embedded scalar.
     */
    fun <R : Any> octonionLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Octonion<R>> =
        octonionTier(base).scalarAction

    /**
     * Octonions over a field, with reciprocals on the units.
     *
     * `o` is invertible iff `N(o)` is invertible in [field], and then `o⁻¹ = conj(o) / N(o)`, a
     * two-sided inverse despite nonassociativity because the algebra is alternative.
     * [isZero] decides when a norm counts as zero; over `Real` a numerical test should be supplied
     * (see [OctonionDivisionAlgebra]).
     */
    fun <F : Any> octonionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): OctonionRingWithReciprocal<F> =
        OctonionRingWithReciprocal(field, isZero)

    class OctonionRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : NonAssociativeInvolutiveRing<Octonion<F>> by octonionRing(field),
        HasNormSq<Octonion<F>, F>,
        HasReciprocal<Octonion<F>> {

        private val leftAction = octonionLeftAction(field)

        override val zero: Octonion<F>
            get() = add.identity

        override val normSq: UnaryOp<Octonion<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Octonion<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Octonion<F>> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the octonions")
            leftAction(field.reciprocal(normSq(o)), conj(o))
        }
    }

    /**
     * `Octonion<R>` as a free `R`-module of rank 8.
     */
    fun <R : Any> octonionModule(
        base: CommutativeRing<R>
    ): RModule<R, Octonion<R>> =
        RModule.of(
            scalars = base,
            add = octonionRing(base).add,
            leftAction = octonionLeftAction(base)
        )

    /**
     * `Octonion<R>` as a nonassociative `R`-algebra.
     */
    fun <R : Any> octonionAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeAlgebra<R, Octonion<R>> =
        NonAssociativeAlgebra.of(
            scalars = base,
            algebraRing = octonionRing(base),
            leftAction = octonionLeftAction(base)
        )

    /**
     * `Octonion<R>` as a nonassociative `R`-star-algebra under the Cayley-Dickson conjugation.
     */
    fun <R : Any> octonionStarAlgebra(
        base: CommutativeRing<R>
    ): NonAssociativeStarAlgebra<R, Octonion<R>> =
        NonAssociativeStarAlgebra.of(
            scalars = base,
            involutiveRing = octonionRing(base),
            leftAction = octonionLeftAction(base)
        )

    /**
     * The embedding of the base ring as the scalars.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<R, Octonion<R>> {
        val tier = octonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = base,
            codomain = tier.ring,
            map = tier.scalarEmbeddingMap
        )
    }

    /**
     * The canonical Cayley-Dickson embedding `Quaternion<R> ↪ Octonion<R>`, `q ↦ (q, 0)`:
     * `i ↦ e₁`, `j ↦ e₂`, `k ↦ e₃`.
     */
    fun <R : Any> quaternionToOctonionMonomorphism(
        base: CommutativeRing<R>
    ): NonAssociativeRingMonomorphism<Quaternion<R>, Octonion<R>> {
        val tier = octonionTier(base)
        return NonAssociativeRingMonomorphism.of(
            domain = tier.quaternions.ring,
            codomain = tier.ring,
            map = tier.canonicalEmbeddingMap
        )
    }

    /**
     * Coordinatewise equality on octonions over any base.
     */
    fun <R : Any> eqOctonion(base: CommutativeRing<R>, eqR: Eq<R>): Eq<Octonion<R>> =
        octonionTier(base).eq(eqR)

    /**
     * The printable in the basis `1, e₁, …, e₇` over any base, dropping zero terms.
     */
    fun <R : Any> printableOctonion(
        base: CommutativeRing<R>,
        signed: SignedOps<R>,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<Octonion<R>> =
        octonionTier(base).printable(signed, prR, eqR)

    // ── The real octonions 𝕆 ─────────────────────────────────────────────────

    /**
     * The real octonions: a non-associative (but alternative) real normed division algebra.
     *
     * Everything but the reciprocal comes from the Cayley-Dickson double of ℍ. Reciprocals follow
     * the floating-point policy of [HasReciprocal]: `o` is invertible iff `N(o)` is finite and not
     * approximately zero, and then `o⁻¹ = conj(o) / N(o)`; this works despite nonassociativity
     * because the norm is a composition norm.
     */
    object OctonionDivisionAlgebra :
        NonAssociativeInvolutiveRing<RealOctonion> by realOctonionTier.ring,
        RealNormedDivisionAlgebra<RealOctonion> {

        override val zero: RealOctonion = realOctonionTier.ring.zero
        override val one: RealOctonion = realOctonionTier.ring.one

        override val normSq: UnaryOp<RealOctonion, Real> =
            realOctonionTier.normSq

        override fun hasReciprocal(a: RealOctonion): Boolean {
            val n2 = normSq(a)
            return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
        }

        override val reciprocal: Endo<RealOctonion> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, Symbols.BB_O)
            realOctonionTier.scalarAction(1.0 / normSq(o), conj(o))
        }

        /**
         * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of ℍ:
         * `e₁, e₂, e₃` are `i, j, k` in the first slot and `e₄, …, e₇` are `1, i, j, k` in the second.
         */
        val basis: List<RealOctonion> = realOctonionTier.basis
        val basisMap: Map<Int, RealOctonion> = realOctonionTier.basisMap

        val e1: RealOctonion = basis[1]
        val e2: RealOctonion = basis[2]
        val e3: RealOctonion = basis[3]
        val e4: RealOctonion = basis[4]
        val e5: RealOctonion = basis[5]
        val e6: RealOctonion = basis[6]
        val e7: RealOctonion = basis[7]
    }

    /**
     * The octonions as an eight-dimensional real vector space. The reals lie in the nucleus (indeed
     * the center) of 𝕆, so scalar multiplication is well-behaved despite nonassociativity.
     */
    val OctonionVectorSpace: FiniteVectorSpace<Real, RealOctonion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = OctonionDivisionAlgebra.add,
        dimension = 8,
        leftAction = realOctonionTier.scalarAction
    )

    val OctonionStarAlgebra: NonAssociativeStarAlgebra<Real, RealOctonion> = NonAssociativeStarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = OctonionDivisionAlgebra,
        leftAction = OctonionVectorSpace.leftAction,
    )

    /**
     * The canonical Cayley-Dickson embedding `ℍ ↪ 𝕆`, `q ↦ (q, 0)`.
     */
    val QuaternionToOctonionMonomorphism: NonAssociativeRingMonomorphism<RealQuaternion, RealOctonion> =
        NonAssociativeRingMonomorphism.of(
            domain = QuaternionAlgebras.QuaternionDivisionRing,
            codomain = OctonionDivisionAlgebra,
            map = realOctonionTier.canonicalEmbeddingMap
        )

    /**
     * Embed a quaternion into the octonions via the canonical Cayley-Dickson embedding `q ↦ (q, 0)`.
     * This is the member of [allQuaternionEmbeddings] with `i ↦ e₁`, `j ↦ e₂` and both signs positive.
     */
    fun RealQuaternion.asOctonion(): RealOctonion =
        QuaternionToOctonionMonomorphism(this)

    val eqOctonionStrict: Eq<RealOctonion> = realOctonionTier.eq(RealAlgebras.eqRealStrict)
    val eqOctonion: Eq<RealOctonion> = realOctonionTier.eq(RealAlgebras.eqRealApprox)

    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<Real, RealQuaternion, RealOctonion> =
        realOctonionTier.embeddingKit(eqOctonionStrict)

    /**
     * The 168 signed-basis-unit embeddings `ℍ ↪ 𝕆`: 42 ordered pairs of distinct imaginary units
     * for the images of `i` and `j`, times 4 independent sign choices. See [OctonionEmbeddingKit].
     */
    fun allQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RealQuaternion, RealOctonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     *
     * Every one of the 168 embeddings in [allQuaternionEmbeddings] is either normalized or a
     * normalized embedding precomposed with the quaternion automorphism `i ↦ i, j ↦ -j, k ↦ -k`,
     * so this is a set of representatives for the embeddings modulo that automorphism.
     */
    fun normalizedQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RealQuaternion, RealOctonion>> =
        embeddingKit.normalizedEmbeddings()

    val printableOctonion: Printable<RealOctonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableOctonionStrict: Printable<RealOctonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableOctonionPretty: Printable<RealOctonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
