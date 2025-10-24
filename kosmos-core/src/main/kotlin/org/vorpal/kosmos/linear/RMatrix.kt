package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Ring

/**
 * The most general form of a matrix over a ring.
 */
open class RMatrix<R>(
    val rows: Int,
    val cols: Int,
    val ring: Ring<R>,
    private val data: List<List<R>>,
) {
    // m and n notation also available for added simplicity.
    val m: Int
        get() = rows
    val n: Int
        get() = cols

    operator fun get(i: Int, j: Int): R = data[i][j]

    fun map(f: (R) -> R): RMatrix<R> =
        RMatrix(rows, cols, ring, data.map { row -> row.map(f) })

    fun transpose(): RMatrix<R> =
        RMatrix(cols, rows, ring, (0 until cols).map { j -> (0 until rows).map { i -> data[i][j] } })

    /** Matrix addition over the field. */
    operator fun plus(other: RMatrix<R>): RMatrix<R> {
        require(rows == other.rows && cols == other.cols)
        val sum = List(rows) { i ->
            List(cols) { j ->
                ring.add.op.combine(this[i, j], other[i, j])
            }
        }
        return RMatrix(rows, cols, ring, sum)
    }

    /** Matrix multiplication over the field. */
    operator fun times(other: RMatrix<R>): RMatrix<R> {
        require(cols == other.rows)
        val product = List(rows) { i ->
            List(other.cols) { j ->
                var sum = ring.add.identity
                for (k in 0 until cols) {
                    val term = ring.mul.op.combine(this[i, k], other[k, j])
                    sum = ring.add.op.combine(sum, term)
                }
                sum
            }
        }
        return RMatrix(rows, other.cols, ring, product)
    }

    override fun toString(): String =
        data.joinToString("\n") { row -> row.joinToString(" ") }
}
