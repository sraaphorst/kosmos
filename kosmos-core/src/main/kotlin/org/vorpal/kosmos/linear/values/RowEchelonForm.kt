package org.vorpal.kosmos.linear.values

/**
 * Output format for a matrix in row echelon form or reduced row echelon form.
 *
 * @property matrix The resulting echelon-form matrix.
 * @property pivots Pivot positions as `(row, column)` pairs.
 * @property rowSwaps Number of row swaps performed during reduction.
 */
data class RowEchelonForm<A : Any>(
    val matrix: DenseMat<A>,
    val pivots: List<Pair<Int, Int>>,
    val rowSwaps: Int
) {
    init {
        // Ensure that pivot rows and columns are within matrix bounds.
        pivots.forEach { (row, col) ->
            require(row in 0 until matrix.rows  && col in 0 until matrix.cols) {
                "Pivot ($row, $col) out of matrix bounds: rows=${matrix.rows}, cols=${matrix.cols}"
            }
        }

        // Ensure pivot rows and columns are strictly increasing.
        pivots.zipWithNext().forEach { (prev, next) ->
            require(next.first > prev.first) {
                "Pivot rows must be strictly increasing: $prev before $next"
            }
            require(next.second > prev.second) {
                "Pivot columns must be strictly increasing: $prev before $next"
            }
        }
    }

    val rank: Int = pivots.size
}
