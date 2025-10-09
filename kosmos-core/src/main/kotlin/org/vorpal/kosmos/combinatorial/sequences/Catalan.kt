package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.NonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence

/**
 * Represents the infinite sequence of **Catalan numbers** Cₙ.
 *
 * These count a wide variety of combinatorial objects, including:
 * - the number of correct bracketings of n+1 factors;
 * - the number of rooted binary trees with n+1 leaves; and
 * - the number of monotonic lattice paths along the edges of an n×n grid
 *   that do not cross the diagonal.
 *
 * ### Definition
 * The Catalan numbers are defined by:
 * ```
 * Cₙ = (1 / (n + 1)) * (2n choose n)
 * ```
 *
 * ### Equivalent factorial form
 * ```
 * Cₙ = (2n)! / ((n + 1)! * n!)
 * ```
 *
 * ### Recurrence relation
 * ```
 * C₀ = 1
 * C_{n+1} = Σ_{i=0}^{n} C_i * C_{n-i}
 * ```
 *
 * ### Example
 * ```
 * C₀ = 1
 * C₁ = 1
 * C₂ = 2
 * C₃ = 5
 * C₄ = 14
 * ```
 *
 * Every [Recurrence] is a [Sequence], so this can be treated as such.
 * We delegate the things Recurrence needs (initial, iterator()) to NonlinearRecurrence.
 */
object Catalan : Recurrence<BigInteger> by NonlinearRecurrence(
    initial = listOf(BigInteger.ONE),

    // Since each new term depends on all earlier ones, they are already implicitly stored in generator's
    // internal list: no explicit memoization needed, which is only useful for random access.
    next = { terms ->
        val n = terms.lastIndex
        var acc = BigInteger.ZERO
        for (i in 0..n) acc += terms[i] * terms[n - i]
        acc
    }
)
