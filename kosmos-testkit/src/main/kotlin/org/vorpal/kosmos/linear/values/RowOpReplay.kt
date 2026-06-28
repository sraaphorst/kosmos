package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.algebra.structures.Field

/**
 * Replay a sequence of [RowOp]s on a starting matrix, returning the resulting matrix.
 *
 * This is the **inverse direction** of the trace API: the trace-producing variants of
 * `rowEchelonForm` / `reducedRowEchelonForm` promise that starting from the input matrix
 * and applying the recorded operations in order reproduces the returned form's matrix.
 * This function lets you verify that contract.
 *
 * Semantics of each operation:
 *  - [RowOp.Swap]`(i, j)` — swap rows `i` and `j`.
 *  - [RowOp.Scale]`(row, factor)` — multiply every entry of `row` by `factor`.
 *  - [RowOp.AddMultiple]`(src, dst, factor)` — add `factor * src` row to `dst` row.
 */
fun <A : Any> replay(
    field: Field<A>,
    mat: MatLike<A>,
    ops: List<RowOp<A>>
): DenseMat<A> {
    val rows = (0 until mat.rows).map { r ->
        (0 until mat.cols).map { c -> mat[r, c] }.toMutableList()
    }.toMutableList()

    ops.forEach { op ->
        when (op) {
            is RowOp.Swap<A> -> {
                val tmp = rows[op.i]
                rows[op.i] = rows[op.j]
                rows[op.j] = tmp
            }
            is RowOp.Scale<A> -> {
                for (c in 0 until mat.cols) {
                    rows[op.row][c] = field.mul(rows[op.row][c], op.factor)
                }
            }
            is RowOp.AddMultiple<A> -> {
                for (c in 0 until mat.cols) {
                    rows[op.dst][c] = field.add(
                        rows[op.dst][c],
                        field.mul(op.factor, rows[op.src][c])
                    )
                }
            }
        }
    }

    return DenseMat.ofRows(rows)
}
