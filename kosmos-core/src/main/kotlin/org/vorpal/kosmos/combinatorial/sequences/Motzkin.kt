package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.CachedNonlinearSequence
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
object Motzkin : CachedNonlinearSequence(
    initial = listOf(BigInteger.ONE, BigInteger.ONE),
    next = { prefix ->
        val n = prefix.lastIndex
        (0..n).fold(prefix[n]) { acc, i ->
            acc + prefix[i] * prefix[n - 1 - i]
        }
    }
),
    CachedClosedForm {
    override fun closedFormCalculator(n: Int): BigInteger =
        // Mₙ = Σ_{k=0}^{⌊n/2⌋} (1 / (k+1)) * binom(n, 2k) * binom(2k, k)
        (0..(n/2)).fold(BigInteger.ZERO) { acc, k ->
            acc + Binomial(n, 2 * k) * Binomial(2 * k, k) / BigInteger.valueOf(k + 1L)
        }
}