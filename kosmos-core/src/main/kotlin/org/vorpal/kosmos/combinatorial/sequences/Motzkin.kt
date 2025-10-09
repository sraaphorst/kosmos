package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.NonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * Infinite sequence of **Motzkin numbers** Mₙ.
 *
 * Motzkin numbers count the number of lattice paths from (0,0) to (n,0)
 * using steps (1,1), (1,0), and (1,-1) that never go below the x-axis.
 *
 * Equivalently:
 * - The number of non-crossing chord diagrams on n points.
 * - The number of ways to draw non-intersecting chords among n labeled points on a circle.
 *
 * Recurrence:
 *   - M₀ = 1, M₁ = 1
 *   - M_{n+1} = M_n + Σ_{i=0}^{n-1} M_i · M_{n-1-i}
 *
 * First few terms:
 *   - 1, 1, 2, 4, 9, 21, 51, 127, 323, ...
 *
 * Related:
 * - Catalan numbers (restricted paths without horizontal steps)
 * - Schröder numbers (paths allowing larger horizontal steps)
 */
object Motzkin : Recurrence<BigInteger> by NonlinearRecurrence(
    initial = listOf(BigInteger.ONE, BigInteger.ONE),
    next = { terms ->
        val n = terms.lastIndex         // we have M₀..Mₙ; compute M_{n+1}
        var acc = terms[n]              // M_n
        for (i in 0 until n) {
            acc += terms[i] * terms[n - 1 - i]
        }
        acc
    }
)
