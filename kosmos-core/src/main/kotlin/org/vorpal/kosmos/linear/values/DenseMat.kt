package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.core.Symbols

/**
 * A dense matrix with [rows] x [cols] entries stored flat in row-major order.
 *
 * This type is an immutable value container. It does not store algebraic structure
 * (no ring/field). Algebraic operations are provided by structure instances built
 * in `org.vorpal.kosmos.algebra.instances`.
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
): MatLike<A> {
    init {
        val size = rows * cols
        require(rows >= 0) { "rows must be nonnegative: $rows" }
        require(cols >= 0) { "cols must be nonnegative: $cols" }
        require(data.size == size) {
            "data size mismatch: expected ${size}, got ${data.size}"
        }
    }

    /**
     * Return the element at row [r], column [c].
     *
     * @throws IndexOutOfBoundsException if indices are out of range.
     */
    @Suppress("UNCHECKED_CAST")
    override operator fun get(r: Int, c: Int): A =
        data[indexOf(r, c)] as A

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

    operator fun iterator(): Iterator<A> =
        object : Iterator<A> {
            private var idx = 0

            override fun hasNext(): Boolean =
                idx < rows * cols

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
     * Zip two matrices entrywise.
     */
    fun <B : Any, C : Any> zipWith(other: DenseMat<B>, f: (A, B) -> C): DenseMat<C> {
        require(rows == other.rows && cols == other.cols) {
            "shape mismatch: ${rows}${Symbols.TIMES}${cols} versus ${other.rows}${Symbols.TIMES}${other.cols}"
        }
        return tabulate(rows, cols) { r, c ->
            f(
                get(r, c),
                other[r, c]
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DenseMat<*>) return false
        if (rows != other.rows) return false
        if (cols != other.cols) return false
        return data.indices.all { data[it] == other.data[it] }
    }

    override fun hashCode(): Int =
        data.indices.fold((31 + rows) * 31 + cols) { h, idx ->
            31 * h + (data[idx]?.hashCode() ?: 0)
        }

    override fun toString(): String =
        toListOfRows().toString()

    /**
     * Convert a coordinate pair (r, c) into a position in the flat data array.
      */
    private fun indexOf(r: Int, c: Int): Int =
        r * cols + c

    companion object {
        /**
         * An unsafe constructor from an already-filled array.
         * This allows us to build results of multiplication without per-entry lambdas.
         */
        internal fun <A : Any> fromArrayUnsafe(
            rows: Int,
            cols: Int,
            data: Array<Any?>
        ): DenseMat<A> =
            DenseMat(rows, cols, data)

        fun <A : Any> tabulate(
            rows: Int,
            cols: Int,
            f: (Int, Int) -> A
        ): DenseMat<A> {
            require(rows >= 0) { "rows must be nonnegative: $rows" }
            require(cols >= 0) { "cols must be nonnegative: $cols" }

            val size = rows * cols
            val data = arrayOfNulls<Any?>(size)
            data.indices.forEach { idx ->
                val row = idx / cols
                val col = idx % cols
                data[idx] = f(row, col)
            }
            return DenseMat(rows, cols, data)
        }

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
