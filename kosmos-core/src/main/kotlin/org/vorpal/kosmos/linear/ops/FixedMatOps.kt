package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeRng
import org.vorpal.kosmos.algebra.structures.Hemiring
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2

/**
 * Operations on the fixed-size matrix [Mat2], written out as closed formulas.
 *
 * Throughout, a matrix is
 * ```text
 *     ⎛ a  b ⎞
 * M = ⎜      ⎟
 *     ⎝ c  d ⎠
 * ```
 * Each function asks for the weakest structure it needs:
 * - addition and multiplication of entries ([matAdd], [matMul], [scale], [matVec], [vecMat],
 *   [trace]) need only a [Hemiring], so they work over semirings (for example tropical ones),
 *   rngs and rings alike;
 * - [matNegate] needs additive inverses, so an [Rng];
 * - [det] and [adjugate] are only meaningful over a commutative rng, where they satisfy
 *   `M · adj(M) = adj(M) · M = det(M) · I`.
 *
 * When the entries do not commute, products keep the order of the factors: the entries of `M · N`
 * are `mᵢ₀n₀ⱼ + mᵢ₁n₁ⱼ`, never `n₀ⱼmᵢ₀ + …`.
 *
 * These agree with the general [DenseMatOps] on a [Mat2] (which is a
 * [org.vorpal.kosmos.linear.values.MatLike]); the difference is that the results here are again
 * [Mat2] and [Vec2], with no dimension checks or allocation of arrays. The algebraic structures
 * built from these operations are in [org.vorpal.kosmos.linear.instances.FixedMatAlgebras].
 */
object FixedMatOps {

    /**
     * The zero matrix `[[0, 0], [0, 0]]` of [hemiring].
     */
    fun <A : Any> zero(
        hemiring: Hemiring<A>
    ): Mat2<A> =
        hemiring.zero.let { o -> Mat2(o, o, o, o) }

    /**
     * The identity matrix `I = [[1, 0], [0, 1]]` of [semiring].
     */
    fun <A : Any> identity(
        semiring: Semiring<A>
    ): Mat2<A> =
        Mat2(semiring.one, semiring.zero, semiring.zero, semiring.one)

    /**
     * The entrywise sum `M + N`.
     */
    fun <A : Any> matAdd(
        hemiring: Hemiring<A>,
        m: Mat2<A>,
        n: Mat2<A>
    ): Mat2<A> =
        hemiring.add.run {
            Mat2(
                invoke(m.a, n.a), invoke(m.b, n.b),
                invoke(m.c, n.c), invoke(m.d, n.d)
            )
        }

    /**
     * The entrywise negation `−M`.
     */
    fun <A : Any> matNegate(
        rng: Rng<A>,
        m: Mat2<A>
    ): Mat2<A> =
        rng.add.inverse.let { neg ->
            Mat2(neg(m.a), neg(m.b), neg(m.c), neg(m.d))
        }

    /**
     * The scalar multiple `s·M`, multiplying each entry by [s] on the left: `(s·M)ᵢⱼ = s·mᵢⱼ`.
     */
    fun <A : Any> scale(
        hemiring: Hemiring<A>,
        s: A,
        m: Mat2<A>
    ): Mat2<A> =
        hemiring.mul.run {
            Mat2(
                invoke(s, m.a), invoke(s, m.b),
                invoke(s, m.c), invoke(s, m.d)
            )
        }

    /**
     * The matrix product
     * ```text
     * ⎛ a  b ⎞ ⎛ e  f ⎞   ⎛ ae + bg   af + bh ⎞
     * ⎝ c  d ⎠ ⎝ g  h ⎠ = ⎝ ce + dg   cf + dh ⎠
     * ```
     * It is associative and has [identity] as its unit; it is not commutative.
     */
    fun <A : Any> matMul(
        hemiring: Hemiring<A>,
        m: Mat2<A>,
        n: Mat2<A>
    ): Mat2<A> =
        hemiring.run {
            Mat2(
                add(mul(m.a, n.a), mul(m.b, n.c)), add(mul(m.a, n.b), mul(m.b, n.d)),
                add(mul(m.c, n.a), mul(m.d, n.c)), add(mul(m.c, n.b), mul(m.d, n.d))
            )
        }

    /**
     * The product `M·v` of a matrix with a column vector:
     * ```text
     * ⎛ a  b ⎞ ⎛ x ⎞   ⎛ ax + by ⎞
     * ⎝ c  d ⎠ ⎝ y ⎠ = ⎝ cx + dy ⎠
     * ```
     * This makes column vectors a *left* module over the matrices: `(MN)·v = M·(N·v)`.
     */
    fun <A : Any> matVec(
        hemiring: Hemiring<A>,
        m: Mat2<A>,
        v: Vec2<A>
    ): Vec2<A> =
        hemiring.run {
            Vec2(
                add(mul(m.a, v.x), mul(m.b, v.y)),
                add(mul(m.c, v.x), mul(m.d, v.y))
            )
        }

    /**
     * The product `v·M` of a row vector with a matrix:
     * ```text
     *          ⎛ a  b ⎞
     * (x  y) · ⎝ c  d ⎠ = (xa + yc,  xb + yd)
     * ```
     * This makes row vectors a *right* module over the matrices: `v·(MN) = (v·M)·N`.
     */
    fun <A : Any> vecMat(
        hemiring: Hemiring<A>,
        v: Vec2<A>,
        m: Mat2<A>
    ): Vec2<A> =
        hemiring.run {
            Vec2(
                add(mul(v.x, m.a), mul(v.y, m.c)),
                add(mul(v.x, m.b), mul(v.y, m.d))
            )
        }

    /**
     * The transpose `Mᵀ = [[a, c], [b, d]]`.
     *
     * Over a commutative ring it reverses products, `(MN)ᵀ = NᵀMᵀ`. For the lazy, general-shape
     * version see [org.vorpal.kosmos.linear.views.transposeView].
     */
    fun <A : Any> transpose(
        m: Mat2<A>
    ): Mat2<A> =
        Mat2(m.a, m.c, m.b, m.d)

    /**
     * The trace `tr(M) = a + d`.
     *
     * Over a commutative ring it is additive and satisfies `tr(MN) = tr(NM)`. This is the closed
     * form of [DenseMatOps.trace].
     */
    fun <A : Any> trace(
        hemiring: Hemiring<A>,
        m: Mat2<A>
    ): A =
        hemiring.add(m.a, m.d)

    /**
     * The determinant `det(M) = ad − bc`.
     *
     * Over a commutative rng it is multiplicative, `det(MN) = det(M)·det(N)`, and `M` is a unit of
     * the matrix ring exactly when `det(M)` is a unit of the entries. This is the closed form of
     * [DenseMatOps.det].
     */
    fun <A : Any> det(
        rng: CommutativeRng<A>,
        m: Mat2<A>
    ): A =
        rng.run {
            add(mul(m.a, m.d), add.inverse(mul(m.b, m.c)))
        }

    /**
     * The adjugate (classical adjoint): swap the diagonal and negate the off-diagonal entries,
     * ```text
     *     ⎛ a  b ⎞   ⎛  d  −b ⎞
     * adj ⎝ c  d ⎠ = ⎝ −c   a ⎠
     * ```
     * It is the transpose of the cofactor matrix, and over a commutative rng:
     * - `M · adj(M) = adj(M) · M = det(M) · I`, so `M⁻¹ = adj(M) / det(M)` whenever `det(M)` is a
     *   unit, with no division needed to define `adj(M)` itself;
     * - `M + adj(M) = tr(M) · I`;
     * - `adj(MN) = adj(N) · adj(M)` and `adj(adj(M)) = M`, so it is an involutive
     *   anti-automorphism: the conjugation of `M₂(R)` seen as the split-quaternions.
     *
     * Not to be confused with the *adjoint* (conjugate transpose) `M*`, which is
     * [org.vorpal.kosmos.linear.views.TransposeMode.ConjugateTranspose].
     */
    fun <A : Any> adjugate(
        rng: CommutativeRng<A>,
        m: Mat2<A>
    ): Mat2<A> =
        rng.add.inverse.let { neg ->
            Mat2(m.d, neg(m.b), neg(m.c), m.a)
        }
}
