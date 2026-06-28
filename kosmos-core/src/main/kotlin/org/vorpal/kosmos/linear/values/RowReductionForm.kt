package org.vorpal.kosmos.linear.values

/**
 * A matrix in a row-reduction form, either:
 * - [RowEchelonForm] (REF); or
 * - [ReducedRowEchelonForm] (RREF).
 */
sealed interface RowReductionForm<A : Any> {
    val matrix: DenseMat<A>
    val pivots: List<Pair<Int, Int>>
    val rowSwaps: Int

    val rank: Int
        get() = pivots.size
}

/**
 * A matrix in normalized row echelon form (REF), i.e. one where:
 * - Each pivot is the first non-zero entry in its row and is normalized to one;
 * - Each pivot is strictly to the right of the pivot above it;
 * - All entries below each pivot are zero;
 * - Zero rows, if any, occur at the bottom.
 *
 * Members:
 * - [matrix]: The matrix in row echelon form.
 * - [pivots]: The list of pivot positions in the matrix.
 * - [rowSwaps]: The number of row swaps performed during the reduction process.
 * - [rank]: The rank of the matrix, equal to `pivots.size`.
 */
data class RowEchelonForm<A : Any>(
    override val matrix: DenseMat<A>,
    override val pivots: List<Pair<Int, Int>>,
    override val rowSwaps: Int
) : RowReductionForm<A> {
    init {
        validateRowReductionForm(matrix, pivots)
    }
}

/**
 * A matrix in reduced row echelon form (RREF), i.e. one where:
 * - Each pivot is the first non-zero entry in its row and is normalized to one;
 * - Each pivot is strictly to the right of the pivot above it;
 * - Each pivot is the only non-zero entry in its column;
 * - Zero rows, if any, occur at the bottom.
 *
 * Members:
 * - [matrix]: The matrix in reduced row echelon form.
 * - [pivots]: The list of pivot positions in the matrix.
 * - [rowSwaps]: The number of row swaps performed during the reduction process.
 * - [rank]: The rank of the matrix, equal to `pivots.size`.
 */
data class ReducedRowEchelonForm<A : Any>(
    override val matrix: DenseMat<A>,
    override val pivots: List<Pair<Int, Int>>,
    override val rowSwaps: Int
) : RowReductionForm<A> {
    init {
        validateRowReductionForm(matrix, pivots)
    }
}


/**
 * A reduction trace is a reduced matrix of type [F] along with the row operations that were applied to it
 * to achieve that reduced form.
 */
data class RowReductionTrace<A : Any, F : RowReductionForm<A>>(
    val form: F,
    val operations: List<RowOp<A>>
)

/**
 * Row operations that can be recorded when reducing a matrix.
 */
sealed interface RowOp<A : Any> {
    data class Swap<A : Any>(
        val i: Int,
        val j: Int
    ) : RowOp<A>

    data class Scale<A : Any>(
        val row: Int,
        val factor: A
    ) : RowOp<A>

    data class AddMultiple<A : Any>(
        val src: Int,
        val dst: Int,
        val factor: A
    ) : RowOp<A>
}

private fun <A : Any> validateRowReductionForm(
    matrix: DenseMat<A>,
    pivots: List<Pair<Int, Int>>
) {
    pivots.forEach { (row, col) ->
        require(row in 0 until matrix.rows && col in 0 until matrix.cols) {
            "Pivot ($row, $col) out of matrix bounds: rows=${matrix.rows}, cols=${matrix.cols}"
        }
    }

    pivots.zipWithNext().forEach { (prev, next) ->
        require(next.first > prev.first) {
            "Pivot rows must be strictly increasing: $prev before $next"
        }
        require(next.second > prev.second) {
            "Pivot columns must be strictly increasing: $prev before $next"
        }
    }
}
