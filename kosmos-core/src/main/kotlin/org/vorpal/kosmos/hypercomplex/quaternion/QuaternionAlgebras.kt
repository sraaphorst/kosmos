package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.RealComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * The quaternion tier over [base]: the usual doubling of `R[i]/(i² + 1)`, on the [Quaternion]
 * carrier.
 */
internal fun <R : Any> quaternionTier(
    base: CommutativeRing<R>
): QuaternionTier<R, Quaternion<R>> =
    QuaternionTier(
        scalars = base,
        wrap = { cd -> Quaternion(cd) },
        unwrap = { q -> q.cd }
    )

/**
 * `ℍ = Quaternion<Real>`: the real quaternion tier.
 *
 * File-level so that initializing [QuaternionAlgebras.QuaternionDivisionRing] never recursively
 * initializes [QuaternionAlgebras].
 */
internal val realQuaternionTier: QuaternionTier<Real, RealQuaternion> =
    quaternionTier(RealAlgebras.RealField)

/**
 * Algebraic structures and utilities for the (Hamilton) quaternions.
 *
 * Generic constructions, over any commutative base ring `R`:
 * - [quaternionRing]: the quaternion ring `(-1, -1)_R`.
 * - [quaternionRingWithReciprocal]: the same over a field, with reciprocals where the norm is
 *   invertible.
 * - [basis]: the standard basis `1, i, j, k`.
 * - [normSq]: the norm `w² + x² + y² + z²`.
 * - [quaternionLeftAction]: the coordinatewise scalar action.
 * - [quaternionModule]: `Quaternion<R>` as a free `R`-module of rank 4.
 * - [quaternionAlgebra], [quaternionStarAlgebra]: as an `R`-(star-)algebra.
 * - [scalarEmbedding]: `R ↪ Quaternion<R>`.
 * - [eqQuaternion], [printableQuaternion] (the overloads taking a base).
 *
 * The arithmetic is not implemented here: it is
 * [org.vorpal.kosmos.algebra.structures.CayleyDickson.associative] applied to `R[i]/(i² + 1)`,
 * transported to the [Quaternion] carrier.
 *
 * The real quaternions ℍ:
 * - [QuaternionDivisionRing]: the quaternion division ring.
 * - [QuaternionVectorSpace]: the four-dimensional vector space of quaternions over the real numbers.
 * - [quaternionLeftComplexVectorSpace]: a two-dimensional left ℂ-vector space using the chosen embedding.
 * - [QuaternionLeftComplexVectorSpacesAll]: a map of all complex embeddings to the vector space that they generate.
 * - [QuaternionLeftComplexVectorSpaceCanonical]: the canonical vector space of quaternions over the complex numbers.
 * - [QuaternionStarAlgebra]: the quaternion star algebra.
 * - [complexEmbedding] (also [ComplexAlgebras.complexToQuaternionEmbedding]): the six unital embeddings ℂ ↪ ℍ.
 * - [QuaternionToComplexMatrixMonomorphism]: ℍ ↪ M₂(ℂ).
 * - [eqQuaternionStrict]: strict equality on real quaternions.
 * - [eqQuaternion]: approximate equality on real quaternions.
 *
 * The rational quaternions and the integral orders have their own objects:
 * [RationalQuaternionAlgebras], [LipschitzQuaternionAlgebras], [HurwitzQuaternionAlgebras].
 */
object QuaternionAlgebras {

    // ── Generic, over a commutative ring ─────────────────────────────────────

    /**
     * The quaternion ring over a commutative base ring: associative, noncommutative, with
     * ```text
     * i² = j² = k² = -1,   ij = k,   jk = i,   ki = j,
     * ```
     * and every pair of distinct units anticommuting. The involution is the Cayley-Dickson
     * conjugation `conj(w + xi + yj + zk) = w - xi - yj - zk`.
     *
     * Whether this is a division ring depends on `R`: it is over ℝ and ℚ, but over ℂ or a finite
     * field the norm is isotropic and there are zero divisors.
     */
    fun <R : Any> quaternionRing(
        base: CommutativeRing<R>
    ): InvolutiveRing<Quaternion<R>> =
        quaternionTier(base).ring

    /**
     * The standard basis `1, i, j, k` over [base].
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Quaternion<R>> =
        quaternionTier(base).basis

    /**
     * The norm `N(q) = q·conj(q) = w² + x² + y² + z²`. It is multiplicative: `N(pq) = N(p)N(q)`.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Quaternion<R>, R> =
        quaternionTier(base).normSq

    /**
     * The coordinatewise scalar action `r ⊳ (w + xi + yj + zk) = rw + rx·i + ry·j + rz·k`.
     */
    fun <R : Any> quaternionLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Quaternion<R>> =
        quaternionTier(base).scalarAction

    /**
     * Quaternions over a field, with reciprocals on the units.
     *
     * `q` is invertible iff `N(q)` is invertible in [field], and then `q⁻¹ = conj(q) / N(q)`. Over a
     * field where the norm is anisotropic (ℚ, ℝ) that is every nonzero `q`.
     * [isZero] decides when a norm counts as zero; over `Real` a numerical test should be supplied
     * (see [QuaternionDivisionRing]).
     */
    fun <F : Any> quaternionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): QuaternionRingWithReciprocal<F> =
        QuaternionRingWithReciprocal(field, isZero)

    class QuaternionRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : InvolutiveRing<Quaternion<F>> by quaternionRing(field),
        HasNormSq<Quaternion<F>, F>,
        HasReciprocal<Quaternion<F>> {

        private val leftAction = quaternionLeftAction(field)

        override val zero: Quaternion<F>
            get() = add.identity

        override val normSq: UnaryOp<Quaternion<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Quaternion<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Quaternion<F>> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, "the quaternions")
            leftAction(field.reciprocal(normSq(q)), conj(q))
        }
    }

    /**
     * `Quaternion<R>` as a free `R`-module of rank 4.
     */
    fun <R : Any> quaternionModule(
        base: CommutativeRing<R>
    ): RModule<R, Quaternion<R>> =
        RModule.of(
            scalars = base,
            add = quaternionRing(base).add,
            leftAction = quaternionLeftAction(base)
        )

    /**
     * `Quaternion<R>` as an `R`-algebra. The scalars are central, so multiplication is `R`-bilinear.
     */
    fun <R : Any> quaternionAlgebra(
        base: CommutativeRing<R>
    ): Algebra<R, Quaternion<R>> =
        Algebra.of(
            scalars = base,
            algebraRing = quaternionRing(base),
            leftAction = quaternionLeftAction(base)
        )

    /**
     * `Quaternion<R>` as an `R`-star-algebra under the Cayley-Dickson conjugation.
     */
    fun <R : Any> quaternionStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, Quaternion<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = quaternionRing(base),
            leftAction = quaternionLeftAction(base)
        )

    /**
     * The embedding of the base ring as the scalars.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, Quaternion<R>> {
        val tier = quaternionTier(base)
        return RingMonomorphism.of(
            domain = base,
            codomain = tier.ring,
            map = tier.scalarEmbeddingMap
        )
    }

    /**
     * Coordinatewise equality on quaternions over any base.
     */
    fun <R : Any> eqQuaternion(base: CommutativeRing<R>, eqR: Eq<R>): Eq<Quaternion<R>> =
        quaternionTier(base).eq(eqR)

    /**
     * The printable `w + xi + yj + zk` over any base, dropping zero terms.
     */
    fun <R : Any> printableQuaternion(
        base: CommutativeRing<R>,
        signed: SignedOps<R>,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<Quaternion<R>> =
        quaternionTier(base).printable(signed, prR, eqR)

    // ── The real quaternions ℍ ───────────────────────────────────────────────

    /**
     * The real quaternions: an associative, noncommutative real normed division algebra.
     *
     * The ring structure is [realQuaternionTier]'s Cayley-Dickson double of ℂ. Reciprocals follow
     * the floating-point policy of [HasReciprocal]: `q` is invertible iff `N(q)` is finite and not
     * approximately zero, and then `q⁻¹ = conj(q) / N(q)`.
     */
    object QuaternionDivisionRing:
        DivisionRing<RealQuaternion>,
        InvolutiveRing<RealQuaternion> by realQuaternionTier.ring,
        RealNormedDivisionAlgebra<RealQuaternion> {

        override val zero: RealQuaternion = realQuaternionTier.ring.zero
        override val one: RealQuaternion = realQuaternionTier.ring.one

        override val normSq: UnaryOp<RealQuaternion, Real> =
            realQuaternionTier.normSq

        override fun hasReciprocal(a: RealQuaternion): Boolean {
            val n2 = normSq(a)
            return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
        }

        override val reciprocal: Endo<RealQuaternion> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, Symbols.BB_H)
            realQuaternionTier.scalarAction(1.0 / normSq(q), conj(q))
        }

        /**
         * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of ℂ.
         */
        val basis: List<RealQuaternion> =
            realQuaternionTier.basis
    }

    /**
     * ℍ as a four-dimensional real vector space; the scalars act coordinatewise.
     */
    val QuaternionVectorSpace: FiniteVectorSpace<Real, RealQuaternion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = QuaternionDivisionRing.add,
        dimension = 4,
        leftAction = realQuaternionTier.scalarAction
    )

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * The unital embedding ℂ ↪ ℍ sending `i_ℂ` to the signed axis chosen by [emb]:
     * ```text
     * a + b i_ℂ ↦ a + b u,   u ∈ {±i, ±j, ±k}
     * ```
     */
    fun complexEmbedding(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<RealComplex, RealQuaternion> =
        RingMonomorphism.of(
            domain = ComplexAlgebras.ComplexField,
            codomain = QuaternionDivisionRing,
            map = realQuaternionTier.complexEmbeddingMap(emb)
        )

    /**
     * ℍ as a left ℂ-vector space using the chosen embedding.
     */
    fun quaternionLeftComplexVectorSpace(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): FiniteVectorSpace<RealComplex, RealQuaternion> {
        val embed = complexEmbedding(emb)
        return FiniteVectorSpace.of(
            scalars = ComplexAlgebras.ComplexField,
            add = QuaternionDivisionRing.add,
            dimension = 2,
            leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { c, q ->
                QuaternionDivisionRing.mul(embed(c), q)
            }
        )
    }

    // A map of all complex embeddings to the vector space that they generate.
    val QuaternionLeftComplexVectorSpacesAll: Map<AxisSignEmbeddings.AxisSignEmbedding, FiniteVectorSpace<RealComplex, RealQuaternion>> =
        AxisSignEmbeddings.AxisSignEmbedding.all.associateWith(::quaternionLeftComplexVectorSpace)

    val QuaternionLeftComplexVectorSpaceCanonical: FiniteVectorSpace<RealComplex, RealQuaternion> =
        quaternionLeftComplexVectorSpace(canonicalEmbedding)

    val QuaternionStarAlgebra: StarAlgebra<Real, RealQuaternion> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = QuaternionDivisionRing,
        leftAction = QuaternionVectorSpace.leftAction
    )

    /**
     * The monomorphism `ℍ ↪ M₂(ℂ)`: writing `q = a + bj` with `a, b ∈ ℂ`,
     * ```text
     * q ↦ [[a, b], [-conj(b), conj(a)]].
     * ```
     */
    object QuaternionToComplexMatrixMonomorphism: RingMonomorphism<RealQuaternion, DenseMat<RealComplex>> {
        private val complexStarAlgebra = ComplexAlgebras.ComplexStarAlgebra
        override val domain = QuaternionDivisionRing
        override val codomain = DenseMatAlgebras.DenseMatRing(complexStarAlgebra, 2)
        override val map: UnaryOp<RealQuaternion, DenseMat<RealComplex>> = UnaryOp { q ->
            val a = Complex(q.cd.a)
            val b = Complex(q.cd.b)
            DenseMat.ofRows(
                listOf(
                    listOf(a, b),
                    listOf(complexStarAlgebra.add.inverse(complexStarAlgebra.conj(b)), complexStarAlgebra.conj(a))
                )
            )
        }
    }

    val eqQuaternionStrict: Eq<RealQuaternion> = realQuaternionTier.eq(RealAlgebras.eqRealStrict)
    val eqQuaternion: Eq<RealQuaternion> = realQuaternionTier.eq(RealAlgebras.eqRealApprox)

    val printableQuaternion: Printable<RealQuaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableQuaternionStrict: Printable<RealQuaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableQuaternionPretty: Printable<RealQuaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
