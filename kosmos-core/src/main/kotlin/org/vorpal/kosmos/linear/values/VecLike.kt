package org.vorpal.kosmos.linear.values

/**
 * Anything that can be read as a vector: a length and an entry at each index.
 *
 * Implemented by the stored vectors ([DenseVec], [Vec0] through [Vec4]) and by the lazy views in
 * [org.vorpal.kosmos.linear.views] (rows and columns of a matrix, slices, the diagonal). The
 * operations in [org.vorpal.kosmos.linear.ops.DenseVecOps] accept any [VecLike], so they work on
 * all of these without copying.
 *
 * Indices are zero-based.
 */
interface VecLike<out A : Any> {
    /**
     * The number of entries.
     */
    val size: Int

    /**
     * The entry at index [i], for `0 ≤ i < size`.
     *
     * @throws IndexOutOfBoundsException (or [IllegalArgumentException] for some views) if [i] is
     * out of range
     */
    operator fun get(i: Int): A
}
