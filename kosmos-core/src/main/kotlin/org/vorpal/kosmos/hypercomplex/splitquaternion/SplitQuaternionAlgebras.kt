package org.vorpal.kosmos.hypercomplex.splitquaternion

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingIsomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
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
import org.vorpal.kosmos.core.render.contramap
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionTier
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * The split-quaternion tier over [base]: the split doubling of `R[i]/(i² + 1)`.
 */
internal fun <R : Any> splitQuaternionTier(
    base: CommutativeRing<R>
): QuaternionTier<R, CD<R>> =
    QuaternionTier.over(base, split = true)

/**
 * Algebraic structures and utilities for the split-quaternions (coquaternions).
 *
 * Main constructions:
 * - [splitQuaternionRing]: the split-quaternion ring `(-1, +1)_R` over a commutative base ring.
 * - [splitQuaternionRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [basis]: the standard basis `1, i, j, k`.
 * - [normSq]: the isotropic norm `w² + x² - y² - z²`.
 * - [splitQuaternionLeftAction]: the coordinatewise scalar action.
 * - [splitQuaternionModule]: `SplitQuaternion<R>` as a free `R`-module of rank 4.
 * - [splitQuaternionAlgebra], [splitQuaternionStarAlgebra]: as an `R`-(star-)algebra.
 *
 * The arithmetic is not implemented here: it is [CayleyDickson.associativeSplit] applied to
 * `R[i]/(i² + 1)`, transported to the [SplitQuaternion] carrier.
 *
 * Real conveniences:
 * - [RealSplitQuaternionRing]: split-quaternions over `Real`, with numerically guarded reciprocals.
 * - [RealSplitQuaternionStarAlgebra]: the real star algebra.
 * - [SplitQuaternionRealVectorSpace]: the four-dimensional real vector space.
 * - [i], [j], [k]: the three units over `Real`.
 *
 * Morphisms:
 * - [scalarEmbedding]: `R ↪ SplitQuaternion<R>`.
 * - [ComplexToSplitQuaternionMonomorphism]: the Cayley-Dickson embedding `ℂ ↪ ℍ'`, `a + bi ↦ a + bi`.
 * - [splitComplexToSplitQuaternionMonomorphism], [splitComplexToSplitQuaternionAlongK]: the
 *   split-complex numbers along `j` and along `k`.
 * - [splitQuaternionToRank2MatrixMonomorphism]: `SplitQuaternion<R> ↪ M₂(R)`.
 * - [splitQuaternionMatrixIsomorphism]: `SplitQuaternion<F> ≅ M₂(F)` when 2 is invertible.
 */
object SplitQuaternionAlgebras {

    private fun <R : Any> transport(): CarrierTransport<CD<CD<R>>, SplitQuaternion<R>> =
        CarrierTransport(
            wrap = UnaryOp { cd -> SplitQuaternion(cd) },
            unwrap = UnaryOp { q -> q.cd }
        )

    /**
     * The split-quaternion ring over a commutative base ring: associative, noncommutative, with
     * ```text
     * i² = -1,   j² = k² = +1,   ij = k,   jk = -i,   ki = j,
     * ```
     * and every pair of distinct units anticommuting. The involution is the Cayley-Dickson
     * conjugation
     * ```text
     * conj(w + xi + yj + zk) = w - xi - yj - zk.
     * ```
     */
    fun <R : Any> splitQuaternionRing(
        base: CommutativeRing<R>
    ): InvolutiveRing<SplitQuaternion<R>> =
        transport<R>().involutiveRing(splitQuaternionTier(base).ring)

    /**
     * The standard basis `1, i, j, k` over [base].
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<SplitQuaternion<R>> =
        splitQuaternionTier(base).basis.map { cd -> SplitQuaternion(cd) }

    private val realBasis: List<SplitQuaternion<Real>> =
        basis(RealAlgebras.RealField)

    /** The elliptic unit over the reals: `i² = -1`. */
    val i: SplitQuaternion<Real> = realBasis[1]

    /** The first hyperbolic unit over the reals: `j² = +1`. */
    val j: SplitQuaternion<Real> = realBasis[2]

    /** The second hyperbolic unit over the reals: `k = ij`, `k² = +1`. */
    val k: SplitQuaternion<Real> = realBasis[3]

    /**
     * The norm
     * ```text
     * N(q) = q·conj(q) = w² + x² - y² - z².
     * ```
     * It is multiplicative, `N(pq) = N(p)N(q)`, and under the matrix isomorphism it is the
     * determinant. It is isotropic: `N(q) = 0` on the null cone, which consists of the zero divisors
     * (and 0).
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<SplitQuaternion<R>, R> {
        val tierNormSq = splitQuaternionTier(base).normSq
        return UnaryOp(Symbols.NORM_SQ_SYMBOL) { q -> tierNormSq(q.cd) }
    }

    /**
     * The coordinatewise scalar action `r ⊳ (w + xi + yj + zk) = rw + rx·i + ry·j + rz·k`.
     */
    fun <R : Any> splitQuaternionLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, SplitQuaternion<R>> {
        val tierAction = splitQuaternionTier(base).scalarAction
        return LeftAction(Symbols.TRIANGLE_RIGHT) { r, q -> SplitQuaternion(tierAction(r, q.cd)) }
    }

    /**
     * Split-quaternions over a field, with reciprocals on the units.
     *
     * `q` is invertible iff `N(q)` is invertible in [field], i.e. iff it is off the null cone, and then
     * ```text
     * q⁻¹ = conj(q) / N(q).
     * ```
     * [isZero] decides when a norm counts as zero; over an exact field the default equality is
     * right, while over `Real` a numerical test should be supplied (see [RealSplitQuaternionRing]).
     */
    fun <F : Any> splitQuaternionRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): SplitQuaternionRingWithReciprocal<F> =
        SplitQuaternionRingWithReciprocal(field, isZero)

    class SplitQuaternionRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : InvolutiveRing<SplitQuaternion<F>> by splitQuaternionRing(field),
        HasNormSq<SplitQuaternion<F>, F>,
        HasReciprocal<SplitQuaternion<F>> {

        private val leftAction = splitQuaternionLeftAction(field)

        override val zero: SplitQuaternion<F>
            get() = add.identity

        override val normSq: UnaryOp<SplitQuaternion<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: SplitQuaternion<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<SplitQuaternion<F>> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, "the split-quaternions")
            leftAction(field.reciprocal(normSq(q)), conj(q))
        }
    }

    /**
     * `SplitQuaternion<R>` as a free `R`-module of rank 4.
     */
    fun <R : Any> splitQuaternionModule(
        base: CommutativeRing<R>
    ): RModule<R, SplitQuaternion<R>> =
        RModule.of(
            scalars = base,
            add = splitQuaternionRing(base).add,
            leftAction = splitQuaternionLeftAction(base)
        )

    /**
     * `SplitQuaternion<R>` as an `R`-algebra. The scalars are central, so multiplication is
     * `R`-bilinear.
     */
    fun <R : Any> splitQuaternionAlgebra(
        base: CommutativeRing<R>
    ): Algebra<R, SplitQuaternion<R>> =
        Algebra.of(
            scalars = base,
            algebraRing = splitQuaternionRing(base),
            leftAction = splitQuaternionLeftAction(base)
        )

    /**
     * `SplitQuaternion<R>` as an `R`-star-algebra under the Cayley-Dickson conjugation.
     */
    fun <R : Any> splitQuaternionStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, SplitQuaternion<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = splitQuaternionRing(base),
            leftAction = splitQuaternionLeftAction(base)
        )

    /**
     * The split-quaternions over the reals, `ℍ' ≅ M₂(ℝ)`.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `q` is invertible iff
     * `N(q) = w² + x² - y² - z²` is finite and not approximately zero.
     */
    object RealSplitQuaternionRing :
        InvolutiveRing<SplitQuaternion<Real>> by splitQuaternionRing(RealAlgebras.RealField),
        HasNormSq<SplitQuaternion<Real>, Real>,
        HasReciprocal<SplitQuaternion<Real>> {

        private val guarded = splitQuaternionRingWithReciprocal(
            field = RealAlgebras.RealField,
            isZero = UnaryOp { n -> !n.isFinite() || RealAlgebras.eqRealApprox(n, 0.0) }
        )

        override val zero: SplitQuaternion<Real>
            get() = add.identity

        override val normSq: UnaryOp<SplitQuaternion<Real>, Real> = guarded.normSq

        override fun hasReciprocal(a: SplitQuaternion<Real>): Boolean =
            guarded.hasReciprocal(a)

        override val reciprocal: Endo<SplitQuaternion<Real>> = guarded.reciprocal

        /**
         * The standard basis `1, i, j, k`.
         */
        val basis: List<SplitQuaternion<Real>> =
            basis(RealAlgebras.RealField)
    }

    /**
     * The split-quaternion star algebra over the reals.
     *
     * Like the split-complex numbers, and unlike ℍ, this is *not* a normed division algebra: the
     * norm form is indefinite and there are zero divisors.
     */
    object RealSplitQuaternionStarAlgebra :
        InvolutiveRing<SplitQuaternion<Real>> by RealSplitQuaternionRing,
        StarAlgebra<Real, SplitQuaternion<Real>> {

        override val zero: SplitQuaternion<Real> = RealSplitQuaternionRing.zero
        override val one: SplitQuaternion<Real> = RealSplitQuaternionRing.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<SplitQuaternion<Real>> = RealSplitQuaternionRing.conj

        override val leftAction: LeftAction<Real, SplitQuaternion<Real>> =
            splitQuaternionLeftAction(RealAlgebras.RealField)
    }

    /**
     * `ℍ'` as a four-dimensional real vector space; the scalars act coordinatewise.
     */
    object SplitQuaternionRealVectorSpace : FiniteVectorSpace<Real, SplitQuaternion<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealSplitQuaternionRing.add
        override val dimension = 4
        override val leftAction: LeftAction<Real, SplitQuaternion<Real>> =
            RealSplitQuaternionStarAlgebra.leftAction
    }

    /**
     * The embedding of the base ring as the scalars (the center, when 2 is not a zero divisor).
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, SplitQuaternion<R>> =
        RingMonomorphism.of(
            domain = base,
            codomain = splitQuaternionRing(base),
            map = UnaryOp { r -> splitQuaternion(r, base.zero, base.zero, base.zero) }
        )

    /**
     * The Cayley-Dickson embedding `ℂ ↪ ℍ'`, `a + bi ↦ a + bi`: the first-slot injection of the
     * split doubling. Its image `{w + xi}` is the only coordinate plane that is a copy of ℂ; the
     * planes through `j` and `k` are split-complex.
     */
    object ComplexToSplitQuaternionMonomorphism : RingMonomorphism<Complex, SplitQuaternion<Real>> {
        override val domain = ComplexAlgebras.ComplexField
        override val codomain = RealSplitQuaternionRing
        override val map: UnaryOp<Complex, SplitQuaternion<Real>> =
            UnaryOp { c -> SplitQuaternion(CD(c, ComplexAlgebras.ComplexField.zero)) }
    }

    /**
     * The embedding of the split-complex numbers along `j`: `a + b·j ↦ a + b·j`.
     */
    fun <R : Any> splitComplexToSplitQuaternionMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<SplitComplex<R>, SplitQuaternion<R>> =
        RingMonomorphism.of(
            domain = SplitComplexAlgebras.splitComplexRing(base),
            codomain = splitQuaternionRing(base),
            map = UnaryOp { (re, hy) -> splitQuaternion(re, base.zero, hy, base.zero) }
        )

    /**
     * The embedding of the split-complex numbers along `k`: `a + b·j ↦ a + b·k`.
     */
    fun <R : Any> splitComplexToSplitQuaternionAlongK(
        base: CommutativeRing<R>
    ): RingMonomorphism<SplitComplex<R>, SplitQuaternion<R>> =
        RingMonomorphism.of(
            domain = SplitComplexAlgebras.splitComplexRing(base),
            codomain = splitQuaternionRing(base),
            map = UnaryOp { (re, hy) -> splitQuaternion(re, base.zero, base.zero, hy) }
        )

    private fun <R : Any> toMatrix(
        base: CommutativeRing<R>
    ): UnaryOp<SplitQuaternion<R>, DenseMat<R>> =
        UnaryOp { (w, x, y, z) ->
            fun minus(a: R, b: R): R = base.add(a, base.add.inverse(b))
            DenseMat.ofRows(
                listOf(
                    listOf(minus(w, z), minus(y, x)),
                    listOf(base.add(x, y), base.add(w, z))
                )
            )
        }

    /**
     * The matrix representation `SplitQuaternion<R> ↪ M₂(R)` determined by
     * ```text
     * i ↦ [[0, -1], [1, 0]],   j ↦ [[0, 1], [1, 0]],   k ↦ [[-1, 0], [0, 1]],
     * ```
     * i.e.
     * ```text
     * w + xi + yj + zk ↦ [[w - z, y - x], [x + y, w + z]].
     * ```
     * The norm becomes the determinant. Over a ring in which 2 is invertible this is onto (see
     * [splitQuaternionMatrixIsomorphism]); over ℤ it is injective but not surjective, since the
     * diagonal entries `w ∓ z` (and the off-diagonal ones) always have equal parity.
     */
    fun <R : Any> splitQuaternionToRank2MatrixMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<SplitQuaternion<R>, DenseMat<R>> =
        RingMonomorphism.of(
            domain = splitQuaternionRing(base),
            codomain = DenseMatAlgebras.DenseMatRing(base, 2),
            map = toMatrix(base)
        )

    /**
     * The isomorphism `SplitQuaternion<F> ≅ M₂(F)` over a field of characteristic other than 2,
     * with inverse
     * ```text
     * [[p, q], [r, s]] ↦ (p + s)/2 + (r - q)/2·i + (q + r)/2·j + (s - p)/2·k.
     * ```
     *
     * @throws IllegalArgumentException if 2 is not invertible in [field].
     */
    fun <F : Any> splitQuaternionMatrixIsomorphism(
        field: Field<F>
    ): RingIsomorphism<SplitQuaternion<F>, DenseMat<F>> {
        val two = field.add(field.one, field.one)
        require(field.hasReciprocal(two)) {
            "The split-quaternions are isomorphic to M₂(F) only when 2 is invertible in F."
        }
        val half = field.reciprocal(two)
        fun halfOf(a: F): F = field.mul(half, a)
        fun minus(a: F, b: F): F = field.add(a, field.add.inverse(b))

        val splitQuaternions = splitQuaternionRing(field)
        val matrices = DenseMatAlgebras.DenseMatRing(field, 2)

        return RingIsomorphism.of(
            forward = RingHomomorphism.of(
                domain = splitQuaternions,
                codomain = matrices,
                map = toMatrix(field)
            ),
            backward = RingHomomorphism.of(
                domain = matrices,
                codomain = splitQuaternions,
                map = UnaryOp { m ->
                    val (p, q) = m[0, 0] to m[0, 1]
                    val (r, s) = m[1, 0] to m[1, 1]
                    splitQuaternion(
                        w = halfOf(field.add(p, s)),
                        x = halfOf(minus(r, q)),
                        y = halfOf(field.add(q, r)),
                        z = halfOf(minus(s, p))
                    )
                }
            )
        )
    }

    /**
     * Eqs
     */
    fun <R : Any> eqSplitQuaternion(eqR: Eq<R>): Eq<SplitQuaternion<R>> =
        CD.eq(CD.eq(eqR)).contramap(SplitQuaternion<R>::cd)

    /**
     * Strict equality on real split-quaternions.
     */
    val eqSplitQuaternionStrict: Eq<SplitQuaternion<Real>> =
        eqSplitQuaternion(RealAlgebras.eqRealStrict)

    /**
     * Approximate equality on real split-quaternions.
     */
    val eqSplitQuaternion: Eq<SplitQuaternion<Real>> =
        eqSplitQuaternion(RealAlgebras.eqRealApprox)

    /**
     * Printables
     */
    fun <R : Any> printableSplitQuaternion(
        base: CommutativeRing<R>,
        signed: SignedOps<R>,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<SplitQuaternion<R>> =
        splitQuaternionTier(base).printable(signed, prR, eqR).contramap(SplitQuaternion<R>::cd)

    private fun printableSplitQuaternionRealGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<SplitQuaternion<Real>> =
        printableSplitQuaternion(RealAlgebras.RealField, RealAlgebras.SignedReal, prReal, eqReal)

    val printableSplitQuaternionReal: Printable<SplitQuaternion<Real>> =
        printableSplitQuaternionRealGenerator(RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableSplitQuaternionStrict: Printable<SplitQuaternion<Real>> =
        printableSplitQuaternionRealGenerator(RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableSplitQuaternionPretty: Printable<SplitQuaternion<Real>> =
        printableSplitQuaternionRealGenerator(RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
