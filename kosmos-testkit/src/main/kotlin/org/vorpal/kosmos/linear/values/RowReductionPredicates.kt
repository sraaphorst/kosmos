package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.core.Eq

/**
 * Structural predicates for matrices in row-echelon or reduced row-echelon form.
 *
 * These check the *intended invariants* of [RowEchelonForm] / [ReducedRowEchelonForm]:
 * the layout of pivots, the values at pivot positions, and the zero-pattern around them.
 * Useful for property tests over arbitrary row-reduction implementations.
 */

/** Every entry of [mat] below each pivot's row, in the pivot's column, equals [zero]. */
fun <A : Any> isBelowPivotsZero(
    mat: MatLike<A>,
    pivots: List<Pair<Int, Int>>,
    zero: A,
    eq: Eq<A> = Eq.default()
): Boolean =
    pivots.all { (pr, pc) ->
        (pr + 1 until mat.rows).all { r -> eq(mat[r, pc], zero) }
    }

/** Every entry of [mat] above each pivot's row, in the pivot's column, equals [zero]. */
fun <A : Any> isAbovePivotsZero(
    mat: MatLike<A>,
    pivots: List<Pair<Int, Int>>,
    zero: A,
    eq: Eq<A> = Eq.default()
): Boolean =
    pivots.all { (pr, pc) ->
        (0 until pr).all { r -> eq(mat[r, pc], zero) }
    }

/** Each pivot entry equals [one]. */
fun <A : Any> arePivotsNormalized(
    mat: MatLike<A>,
    pivots: List<Pair<Int, Int>>,
    one: A,
    eq: Eq<A> = Eq.default()
): Boolean =
    pivots.all { (pr, pc) -> eq(mat[pr, pc], one) }

/** Pivot rows AND columns are both strictly increasing — the canonical staircase shape. */
fun isStaircase(pivots: List<Pair<Int, Int>>): Boolean =
    pivots.zipWithNext().all { (a, b) -> b.first > a.first && b.second > a.second }

/** Every entry to the left of each pivot in its row equals [zero] — the pivot is the leading entry. */
fun <A : Any> isPivotLeading(
    mat: MatLike<A>,
    pivots: List<Pair<Int, Int>>,
    zero: A,
    eq: Eq<A> = Eq.default()
): Boolean =
    pivots.all { (pr, pc) ->
        (0 until pc).all { c -> eq(mat[pr, c], zero) }
    }

/** Zero rows (if any) occur only after the last pivot row. */
fun <A : Any> areZeroRowsAtBottom(
    mat: MatLike<A>,
    pivots: List<Pair<Int, Int>>,
    zero: A,
    eq: Eq<A> = Eq.default()
): Boolean {
    val lastPivotRow = pivots.lastOrNull()?.first ?: -1
    return (lastPivotRow + 1 until mat.rows).all { r ->
        (0 until mat.cols).all { c -> eq(mat[r, c], zero) }
    }
}
