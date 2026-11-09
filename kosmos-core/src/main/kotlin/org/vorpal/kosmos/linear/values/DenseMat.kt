package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.instances.DenseMatKernel

/**
 * A dense matrix with [rows] x [cols] entries stored flat in row-major order.
 *
 * This type is an immutable value container. It does not store algebraic structure
 * (no ring/field). Algebraic operations are provided by structure instances built
 * in `org.vorpal.kosmos.linear.instances`.
 *
 * ### Implementation note:
 *
 * Kotlin/JVM erases generic type parameters, so we store entries in an `Array<Any?>`
 * and cast on access. The constructors ensure no null entries are stored.
 */
class DenseMat<A : Any> private constructor(
    override val rows: Int,
    override val cols: Int,
    private val data: Array<Any?>
): MatLike<A>, Iterable<A> {
    init {
        val size = rows * cols
        require(rows >= 0) { "rows must be nonnegative: $rows" }
        require(cols >= 0) { "cols must be nonnegative: $cols" }
        require(data.size == size) {
            "data size mismatch: expected ${size}, got ${data.size}"
        }
    }

    /**
     * Total number of entries in the matrix.
     */
    val size: Int = data.size

    /**
     * Flat accessors for entries of the matrix, i.e.:
     *
     *    0 <= index < size = rows * cols
     *
     * Passing in an index will return the entry at row and column `index / cols` and `index % cols`.
     *
     * @throws IndexOutOfBoundsException if index is out of range.
     */
    @Suppress("UNCHECKED_CAST")
    fun flatGet(index: Int): A =
        data[index] as A

    /**
     * Return the element at row [r], column [c].
     *
     * Both coordinates are checked separately: checking only the flat index `r * cols + c` would
     * let `[0, cols]` alias `[1, 0]`, or `[1, -1]` alias `[0, cols - 1]`.
     *
     * @throws IndexOutOfBoundsException if either index is out of range.
     */
    override operator fun get(r: Int, c: Int): A {
        if (r !in 0 until rows || c !in 0 until cols)
            throw IndexOutOfBoundsException("Index ($r, $c) out of bounds for a $rows × $cols matrix")
        return flatGet(indexOf(r, c))
    }

    /**
     * The entries as a list of rows, each a list of entries.
     */
    fun toListOfRows(): List<List<A>> =
        List(rows) { r ->
            List(cols) { c ->
                get(r, c)
            }
        }

    /**
     * Map each entry to a new matrix of the same shape.
     */
    fun <B : Any> map(f: (A) -> B): DenseMat<B> =
        tabulate(rows, cols) { r, c -> f(get(r, c)) }

    /**
     * Iterates over the entries in row-major order.
     */
    override operator fun iterator(): Iterator<A> =
        object : Iterator<A> {
            private var idx = 0

            override fun hasNext(): Boolean =
                idx < size

            override fun next(): A {
                if (!hasNext()) throw NoSuchElementException()
                val r = idx / cols
                val c = idx % cols
                val value = get(r, c)
                idx += 1
                return value
            }
        }

    /**
     * Combines this matrix with [other] entrywise: the result has entries `f(this[r, c], other[r, c])`.
     *
     * @throws IllegalArgumentException if the two matrices have different shapes
     */
    fun <B : Any, C : Any> zipWith(other: DenseMat<B>, f: (A, B) -> C): DenseMat<C> {
        require(rows == other.rows && cols == other.cols) {
            "shape mismatch: ${rows}${Symbols.TIMES}${cols} versus ${other.rows}${Symbols.TIMES}${other.cols}"
        }
        return tabulate(rows, cols) { r, c -> f(get(r, c), other[r, c]) }
    }

    /**
     * Two matrices are equal if they have the same shape and equal entries (by [equals]).
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DenseMat<*>) return false
        if (rows != other.rows) return false
        if (cols != other.cols) return false
        return data.indices.all { data[it] == other.data[it] }
    }

    /**
     * A hash code consistent with [equals], combining the shape and the entries.
     */
    override fun hashCode(): Int =
        data.indices.fold((31 + rows) * 31 + cols) { h, idx ->
            31 * h + (data[idx]?.hashCode() ?: 0)
        }

    /**
     * The entries as a list of rows, e.g. `[[1, 2], [3, 4]]`. For configurable rendering see
     * [org.vorpal.kosmos.linear.instances.DenseMatAlgebras.liftPrintablePretty].
     */
    override fun toString(): String =
        toListOfRows().toString()

    /**
     * Convert a coordinate pair (r, c) into a position in the flat data array.
     */
    private fun indexOf(r: Int, c: Int): Int =
        r * cols + c

    companion object {
        /**
         * Wraps an already-filled row-major array without copying it, so the caller must not
         * modify [data] afterwards and must have filled it with non-null values of type [A]. This
         * lets the kernels build results of multiplication without per-entry lambdas.
         */
        internal fun <A : Any> fromArrayUnsafe(
            rows: Int,
            cols: Int,
            data: Array<Any?>
        ): DenseMat<A> =
            DenseMat(rows, cols, data)

        /**
         * The `rows×cols` matrix whose entry at `(r, c)` is `f(r, c)`.
         *
         * @throws IllegalArgumentException if [rows] or [cols] is negative
         * @throws ArithmeticException if `rows × cols` overflows an [Int]
         */
        fun <A : Any> tabulate(
            rows: Int,
            cols: Int,
            f: (Int, Int) -> A
        ): DenseMat<A> {
            require(rows >= 0) { "rows must be nonnegative: $rows" }
            require(cols >= 0) { "cols must be nonnegative: $cols" }

            val data = DenseMatKernel.allocateMatrix(rows, cols)
            data.indices.forEach { idx ->
                val row = idx / cols
                val col = idx % cols
                data[idx] = f(row, col)
            }
            return DenseMat(rows, cols, data)
        }

        /**
         * The matrix with the given rows. An empty list gives the `0×0` matrix.
         *
         * @throws IllegalArgumentException if the rows do not all have the same length
         */
        fun <A : Any> ofRows(
            rows: List<List<A>>
        ): DenseMat<A> {
            val r = rows.size
            val c = if (r == 0) 0 else rows[0].size
            require(rows.all { it.size == c }) { "ragged rows are not allowed" }
            return tabulate(r, c) { i, j -> rows[i][j] }
        }
    }
}
