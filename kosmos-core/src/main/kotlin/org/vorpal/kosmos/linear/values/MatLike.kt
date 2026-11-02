package org.vorpal.kosmos.linear.values

/**
 * Anything that can be read as a matrix: a shape and an entry at each position.
 *
 * Implemented by the stored matrices ([DenseMat], [Mat2], [PermMat]) and by the lazy views in
 * [org.vorpal.kosmos.linear.views] (transposes, slices, a vector seen as a row or column). The
 * operations in [org.vorpal.kosmos.linear.ops.DenseMatOps] accept any [MatLike], so they work on
 * all of these without copying.
 *
 * Indices are zero-based, row first.
 */
interface MatLike<out A : Any> {
    /**
     * The number of rows.
     */
    val rows: Int

    /**
     * The number of columns.
     */
    val cols: Int

    /**
     * The entry in row [r] and column [c], for `0 ≤ r < rows` and `0 ≤ c < cols`.
     *
     * @throws IndexOutOfBoundsException (or [IllegalArgumentException] for some views) if the
     * position is out of range
     */
    operator fun get(r: Int, c: Int): A
}
