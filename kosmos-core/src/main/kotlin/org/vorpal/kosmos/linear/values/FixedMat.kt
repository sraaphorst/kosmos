package org.vorpal.kosmos.linear.values

/**
 * A 2 × 2 matrix with entries of type [A], stored row by row:
 * ```text
 *     ⎛ a  b ⎞
 * M = ⎜      ⎟
 *     ⎝ c  d ⎠
 * ```
 * so `M[0, 0] = a`, `M[0, 1] = b`, `M[1, 0] = c` and `M[1, 1] = d`.
 *
 * This is the fixed-size counterpart of [DenseMat], in the same way that [Vec2] is the fixed-size
 * counterpart of [DenseVec]: the shape is part of the type, so no operation on a [Mat2] needs a
 * dimension check, and the entries can be destructured, `val (a, b, c, d) = m`.
 *
 * Like the other carriers in this package, a [Mat2] holds no algebraic structure. The operations
 * (product, determinant, trace, adjugate, the actions on [Vec2]) are in
 * [org.vorpal.kosmos.linear.ops.FixedMatOps], and the structures built from them (the matrix ring
 * `M₂(R)`, its modules, the star algebra under the adjugate) are in
 * [org.vorpal.kosmos.linear.instances.FixedMatAlgebras].
 *
 * Because [Mat2] is a [MatLike], it can also be passed to anything that accepts a general matrix,
 * for example [org.vorpal.kosmos.linear.ops.DenseMatOps] or the views in
 * [org.vorpal.kosmos.linear.views].
 *
 * @property a the entry in row 0, column 0
 * @property b the entry in row 0, column 1
 * @property c the entry in row 1, column 0
 * @property d the entry in row 1, column 1
 */
data class Mat2<A : Any>(
    val a: A, val b: A,
    val c: A, val d: A
) : MatLike<A> {
    override val rows: Int = 2
    override val cols: Int = 2

    /**
     * The entry in row [r] and column [c].
     *
     * @throws IndexOutOfBoundsException if [r] or [c] is not 0 or 1
     */
    override fun get(r: Int, c: Int): A = when (r) {
        0 -> when (c) {
            0 -> a
            1 -> b
            else -> throw IndexOutOfBoundsException("Invalid index for Mat2: ($r, $c)")
        }
        1 -> when (c) {
            0 -> this.c
            1 -> d
            else -> throw IndexOutOfBoundsException("Invalid index for Mat2: ($r, $c)")
        }
        else -> throw IndexOutOfBoundsException("Invalid index for Mat2: ($r, $c)")
    }
}
