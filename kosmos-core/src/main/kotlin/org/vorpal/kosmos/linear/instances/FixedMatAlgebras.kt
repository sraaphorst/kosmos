package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingIsomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.RSBiModule
import org.vorpal.kosmos.algebra.structures.RightRModule
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.RightAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.linear.ops.FixedMatOps
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2

/**
 * Algebraic structures on the 2 × 2 matrices [Mat2], and on [Vec2] as a module over them.
 *
 * Throughout, a matrix is `M = [[a, b], [c, d]]`, read row by row.
 *
 * The matrices themselves:
 * - [mat2AdditiveMonoid], [mat2AdditiveGroup]: entrywise addition.
 * - [mat2MultiplicativeMonoid]: the matrix product, with identity `I = [[1, 0], [0, 1]]`.
 * - [mat2Semiring], [mat2Ring]: `M₂(S)` over a semiring and `M₂(R)` over a ring. The entries need
 *   not commute; the matrices never do.
 * - [mat2LeftAction], [mat2Module], [mat2Space]: scalars acting entrywise; `M₂(R)` is a free
 *   `R`-module of rank 4.
 * - [mat2Algebra]: `M₂(R)` as an associative `R`-algebra.
 * - [mat2StarAlgebra]: the same, with the adjugate as its involution, which is how `M₂(R)` models
 *   the split-quaternions.
 * - [normSq]: the determinant, the norm of that star algebra: `M · adj(M) = det(M) · I`.
 * - [mat2RingWithReciprocal]: over a field, with reciprocals `M⁻¹ = adj(M) / det(M)` on `GL₂`.
 * - [basis]: the matrix units `E₁₁, E₁₂, E₂₁, E₂₂`.
 *
 * The vectors as modules over the matrices:
 * - [vec2LeftMat2Module]: column vectors, `M ⊳ v = M·v`, a left `M₂(R)`-module.
 * - [vec2RightMat2Module]: row vectors, `v ⊲ M = v·M`, a right `M₂(R)`-module.
 * - [vec2Mat2BiModule]: row vectors as an `(R, M₂(R))`-bimodule, scalars on the left and matrices
 *   on the right.
 *
 * A row vector cannot be made a *left* module by letting `M` act as `v ↦ v·M`: that reverses
 * products, `v·(MN) = (v·M)·N`, so it would be an action of the opposite ring. Each side gets
 * its own structure here for that reason.
 *
 * Morphisms:
 * - [scalarEmbedding]: `R ↪ M₂(R)`, `r ↦ r·I`.
 * - [denseMatIsomorphism]: `Mat2<A> ≅ DenseMat<A>` restricted to 2 × 2, so that results can be
 *   checked against [DenseMatAlgebras.DenseMatRing].
 *
 * Equality and printing:
 * - [eqMat2]: entrywise equality from an equality on the entries.
 * - [printableMat2], [printableMat2Pretty]: one line `[[a, b], [c, d]]`, or two aligned lines.
 *
 * All operations come from [FixedMatOps].
 */
object FixedMatAlgebras {

    // ---------------------------------------
    // ---------- One-operator structures ----
    // ---------------------------------------

    /**
     * The entrywise additive commutative monoid of `Mat2<A>`, built from [monoid] on the entries.
     */
    fun <A : Any> mat2AdditiveMonoid(
        monoid: CommutativeMonoid<A>
    ): CommutativeMonoid<Mat2<A>> = CommutativeMonoid.of(
        identity = monoid.identity.let { o -> Mat2(o, o, o, o) },
        op = BinOp(Symbols.PLUS) { m, n ->
            Mat2(
                monoid(m.a, n.a), monoid(m.b, n.b),
                monoid(m.c, n.c), monoid(m.d, n.d)
            )
        }
    )

    /**
     * The entrywise additive abelian group of `Mat2<A>`, built from [group] on the entries.
     */
    fun <A : Any> mat2AdditiveGroup(
        group: AbelianGroup<A>
    ): AbelianGroup<Mat2<A>> = AbelianGroup.of(
        identity = group.identity.let { o -> Mat2(o, o, o, o) },
        op = BinOp(Symbols.PLUS) { m, n ->
            Mat2(
                group(m.a, n.a), group(m.b, n.b),
                group(m.c, n.c), group(m.d, n.d)
            )
        },
        inverse = Endo(Symbols.MINUS) { m ->
            Mat2(
                group.inverse(m.a), group.inverse(m.b),
                group.inverse(m.c), group.inverse(m.d)
            )
        }
    )

    /**
     * The multiplicative monoid of `Mat2<A>`: the matrix product [FixedMatOps.matMul], with the
     * identity matrix as unit. It is associative whenever [semiring] is, and never commutative
     * (unless [semiring] is trivial).
     */
    fun <A : Any> mat2MultiplicativeMonoid(
        semiring: Semiring<A>
    ): Monoid<Mat2<A>> = Monoid.of(
        identity = FixedMatOps.identity(semiring),
        op = BinOp(Symbols.ASTERISK) { m, n -> FixedMatOps.matMul(semiring, m, n) }
    )

    // ---------------------------------------
    // ---------- Two-operator structures ----
    // ---------------------------------------

    /**
     * The matrix semiring `M₂(S)` over a semiring [semiring]: entrywise addition and the matrix
     * product. Over a tropical semiring this is the semiring used for two-state shortest paths.
     */
    fun <A : Any> mat2Semiring(
        semiring: Semiring<A>
    ): Semiring<Mat2<A>> = Semiring.of(
        add = mat2AdditiveMonoid(semiring.add),
        mul = mat2MultiplicativeMonoid(semiring)
    )

    /**
     * The matrix ring `M₂(R)` over a ring [ring]. The entries need not commute (`M₂(ℍ)` is fine);
     * the matrices themselves never commute, for example `E₁₂ · E₂₁ = E₁₁` but `E₂₁ · E₁₂ = E₂₂`.
     */
    fun <A : Any> mat2Ring(
        ring: Ring<A>
    ): Ring<Mat2<A>> = Ring.of(
        add = mat2AdditiveGroup(ring.add),
        mul = mat2MultiplicativeMonoid(ring)
    )

    // ---------------------------------------
    // ---------- Scalars and modules --------
    // ---------------------------------------

    /**
     * The entrywise scalar action `r ⊳ M = [[ra, rb], [rc, rd]]`, which is also multiplication by
     * the scalar matrix `r·I` on either side.
     */
    fun <R : Any> mat2LeftAction(
        ring: CommutativeRing<R>
    ): LeftAction<R, Mat2<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, m -> FixedMatOps.scale(ring, r, m) }

    /**
     * `M₂(R)` as a free `R`-module of rank 4, with basis [basis].
     */
    fun <R : Any> mat2Module(
        ring: CommutativeRing<R>
    ): RModule<R, Mat2<R>> = RModule.of(
        scalars = ring,
        add = mat2AdditiveGroup(ring.add),
        leftAction = mat2LeftAction(ring)
    )

    /**
     * `M₂(F)` as a four-dimensional vector space over [field], with basis [basis].
     */
    fun <F : Any> mat2Space(
        field: Field<F>
    ): FiniteVectorSpace<F, Mat2<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = mat2AdditiveGroup(field.add),
        dimension = 4,
        leftAction = mat2LeftAction(field)
    )

    /**
     * `M₂(R)` as an associative `R`-algebra: a ring and an `R`-module in which the scalars commute
     * with everything, `(rM)N = r(MN) = M(rN)`.
     */
    fun <R : Any> mat2Algebra(
        ring: CommutativeRing<R>
    ): Algebra<R, Mat2<R>> = Algebra.of(
        scalars = ring,
        algebraRing = mat2Ring(ring),
        leftAction = mat2LeftAction(ring)
    )

    /**
     * `M₂(R)` as an `R`-star-algebra under the adjugate,
     * ```text
     * conj [[a, b], [c, d]] = adj [[a, b], [c, d]] = [[d, −b], [−c, a]].
     * ```
     * The adjugate is an involutive anti-automorphism over any commutative ring:
     * `adj(MN) = adj(N)·adj(M)` and `adj(adj(M)) = M`. With it,
     * - `M + conj(M) = tr(M)·I` (the trace plays the role of twice the real part), and
     * - `M · conj(M) = conj(M) · M = det(M)·I` (the determinant is the norm, see [normSq]),
     *
     * which is exactly the structure of the split-quaternions: when 2 is invertible,
     * `SplitQuaternion<R> ≅ M₂(R)` as star algebras.
     *
     *  The transpose is also an involutive anti-automorphism over a commutative ring, but it is not
     *  the conjugation used here: `M·Mᵀ` is not a scalar matrix in general (for example
     *  `[[1, 1], [0, 1]]·[[1, 0], [1, 1]] = [[2, 1], [1, 1]]`), so it does not define a norm.
     */
    fun <R : Any> mat2StarAlgebra(
        ring: CommutativeRing<R>
    ): StarAlgebra<R, Mat2<R>> = StarAlgebra.of(
        scalars = ring,
        involutiveRing = mat2InvolutiveRing(ring),
        leftAction = mat2LeftAction(ring)
    )

    /**
     * The involutive ring underlying [mat2StarAlgebra].
     */
    private fun <R : Any> mat2InvolutiveRing(
        ring: CommutativeRing<R>
    ): InvolutiveRing<Mat2<R>> = InvolutiveRing.of(
        ring = mat2Ring(ring),
        conj = Endo(Symbols.CONJ) { m -> FixedMatOps.adjugate(ring, m) }
    )

    /**
     * The norm of [mat2StarAlgebra], which is the determinant:
     * ```text
     * N([[a, b], [c, d]]) = ad − bc,    M · adj(M) = N(M)·I.
     * ```
     * It is multiplicative, `N(MN) = N(M)N(N)`, and it is the split norm of signature `(2, 2)`
     * under the split-quaternion picture.
     */
    fun <R : Any> normSq(
        ring: CommutativeRing<R>
    ): UnaryOp<Mat2<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { m -> FixedMatOps.det(ring, m) }

    /**
     * The matrix units, in the order
     * ```text
     * E₁₁ = [[1, 0], [0, 0]],  E₁₂ = [[0, 1], [0, 0]],  E₂₁ = [[0, 0], [1, 0]],  E₂₂ = [[0, 0], [0, 1]].
     * ```
     * They multiply as `Eᵢⱼ · Eₖₗ = δⱼₖ Eᵢₗ`. `E₁₁` and `E₂₂` are orthogonal idempotents with
     * `E₁₁ + E₂₂ = I`, and `E₁₂`, `E₂₁` square to zero; all four have determinant 0.
     */
    fun <R : Any> basis(
        ring: Semiring<R>
    ): List<Mat2<R>> {
        val (o, l) = ring.zero to ring.one
        return listOf(
            Mat2(l, o, o, o),
            Mat2(o, l, o, o),
            Mat2(o, o, l, o),
            Mat2(o, o, o, l)
        )
    }

    // ---------------------------------------
    // ---------- Units ----------------------
    // ---------------------------------------

    /**
     * `M₂(F)` over a field, with reciprocals on the invertible matrices.
     *
     * `M` is invertible iff `det(M)` is nonzero, and then
     * ```text
     * M⁻¹ = adj(M) / det(M).
     * ```
     * [isZero] decides when a determinant counts as zero; over an exact field the default equality
     * is right, while over `Real` a numerical test should be supplied.
     */
    fun <F : Any> mat2RingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): Mat2RingWithReciprocal<F> =
        Mat2RingWithReciprocal(field, isZero)

    /**
     * See [mat2RingWithReciprocal].
     */
    class Mat2RingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : InvolutiveRing<Mat2<F>> by mat2InvolutiveRing(field),
        HasNormSq<Mat2<F>, F>,
        HasReciprocal<Mat2<F>> {

        override val zero: Mat2<F>
            get() = add.identity

        override val normSq: UnaryOp<Mat2<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Mat2<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Mat2<F>> = Endo(Symbols.INVERSE) { m ->
            if (!hasReciprocal(m)) throw HasReciprocal.notInvertible(m, "the 2 × 2 matrices")
            FixedMatOps.scale(field, field.reciprocal(normSq(m)), FixedMatOps.adjugate(field, m))
        }
    }

    // ---------------------------------------
    // ---------- Vec2 over Mat2 -------------
    // ---------------------------------------

    /**
     * Column vectors as a left `M₂(R)`-module: `M ⊳ v = M·v` ([FixedMatOps.matVec]).
     *
     * The module laws hold over any ring [ring], commutative or not:
     * `(MN)·v = M·(N·v)`, `I·v = v`, and the action is additive in each argument.
     */
    fun <A : Any> vec2LeftMat2Module(
        ring: Ring<A>
    ): LeftRModule<Mat2<A>, Vec2<A>> = LeftRModule.of(
        leftScalars = mat2Ring(ring),
        add = FixedVecAlgebras.dim2Group(ring.add),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { m, v -> FixedMatOps.matVec(ring, m, v) }
    )

    /**
     * Row vectors as a right `M₂(R)`-module: `v ⊲ M = v·M` ([FixedMatOps.vecMat]).
     *
     * The module laws hold over any ring [ring], commutative or not:
     * `v·(MN) = (v·M)·N`, `v·I = v`, and the action is additive in each argument.
     */
    fun <A : Any> vec2RightMat2Module(
        ring: Ring<A>
    ): RightRModule<Vec2<A>, Mat2<A>> = RightRModule.of(
        rightScalars = mat2Ring(ring),
        add = FixedVecAlgebras.dim2Group(ring.add),
        rightAction = RightAction(Symbols.TRIANGLE_LEFT) { v, m -> FixedMatOps.vecMat(ring, v, m) }
    )

    /**
     * Row vectors as an `(R, M₂(R))`-bimodule: scalars from [ring] act on the left coordinatewise,
     * and matrices act on the right by [FixedMatOps.vecMat]. The two actions commute,
     * `(r·v)·M = r·(v·M)`, because [ring] is commutative.
     */
    fun <R : Any> vec2Mat2BiModule(
        ring: CommutativeRing<R>
    ): RSBiModule<R, Vec2<R>, Mat2<R>> = RSBiModule.of(
        leftScalars = ring,
        rightScalars = mat2Ring(ring),
        add = FixedVecAlgebras.dim2Group(ring.add),
        leftAction = FixedVecAlgebras.vec2Module(ring).leftAction,
        rightAction = RightAction(Symbols.TRIANGLE_LEFT) { v, m -> FixedMatOps.vecMat(ring, v, m) }
    )

    // ---------------------------------------
    // ---------- Morphisms ------------------
    // ---------------------------------------

    /**
     * The embedding of the base ring as the scalar matrices, `r ↦ r·I = [[r, 0], [0, r]]`. Its
     * image is the center of `M₂(R)`.
     */
    fun <R : Any> scalarEmbedding(
        ring: CommutativeRing<R>
    ): RingMonomorphism<R, Mat2<R>> =
        RingMonomorphism.of(
            domain = ring,
            codomain = mat2Ring(ring),
            map = UnaryOp(Symbols.PHI) { r: R -> Mat2(r, ring.zero, ring.zero, r) }
        )

    /**
     * The identification of [Mat2] with 2 × 2 [DenseMat]s: the same matrix ring stored two ways.
     *
     * The forward map copies the four entries into a [DenseMat]; the backward map reads them out of
     * a 2 × 2 [DenseMat] and rejects any other shape. Both are unital ring homomorphisms over any
     * ring [ring], and they are mutually inverse, so results computed with [FixedMatOps] can be
     * checked against [org.vorpal.kosmos.linear.ops.DenseMatOps].
     */
    fun <A : Any> denseMatIsomorphism(
        ring: Ring<A>
    ): RingIsomorphism<Mat2<A>, DenseMat<A>> {
        val fixed = mat2Ring(ring)
        val dense = DenseMatAlgebras.DenseMatRing(ring, 2)
        return RingIsomorphism.of(
            forward = RingHomomorphism.of(
                domain = fixed,
                codomain = dense,
                map = UnaryOp(Symbols.PHI) { m: Mat2<A> ->
                    DenseMat.ofRows(listOf(listOf(m.a, m.b), listOf(m.c, m.d)))
                }
            ),
            backward = RingHomomorphism.of(
                domain = dense,
                codomain = fixed,
                map = UnaryOp(Symbols.PHI) { m: DenseMat<A> ->
                    DenseMatKernel.requireSize(m, 2, 2)
                    Mat2(m[0, 0], m[0, 1], m[1, 0], m[1, 1])
                }
            )
        )
    }

    // ---------------------------------------
    // ---------- Equality and printing ------
    // ---------------------------------------

    /**
     * Entrywise equality of matrices from an equality [eqA] on the entries.
     */
    fun <A : Any> eqMat2(eqA: Eq<A>): Eq<Mat2<A>> =
        Eq { m, n ->
            eqA(m.a, n.a) && eqA(m.b, n.b) && eqA(m.c, n.c) && eqA(m.d, n.d)
        }

    /**
     * A one-line rendering, row by row: `[[a, b], [c, d]]`, with each entry rendered by [prA].
     */
    fun <A : Any> printableMat2(
        prA: Printable<A>
    ): Printable<Mat2<A>> =
        Printable { m -> "[[${prA(m.a)}, ${prA(m.b)}], [${prA(m.c)}, ${prA(m.d)}]]" }

    /**
     * A two-line rendering with the columns aligned, e.g.
     * ```text
     * ⎛  1  -2 ⎞
     * ⎝ 10   3 ⎠
     * ```
     * with each entry rendered by [prA].
     */
    fun <A : Any> printableMat2Pretty(
        prA: Printable<A>
    ): Printable<Mat2<A>> =
        Printable { m ->
            val (a, b, c, d) = listOf(prA(m.a), prA(m.b), prA(m.c), prA(m.d))
            val leftWidth = maxOf(a.length, c.length)
            val rightWidth = maxOf(b.length, d.length)
            "⎛ ${a.padStart(leftWidth)}  ${b.padStart(rightWidth)} ⎞\n" +
                "⎝ ${c.padStart(leftWidth)}  ${d.padStart(rightWidth)} ⎠"
        }
}
