package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.NonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.combinatorial.Binomial

/**
 * Infinite sequence of **Bell numbers** Bₙ.
 *
 * Bell numbers count the number of ways to partition a set of n elements into
 * non-empty, disjoint subsets (set partitions).
 *
 * Example:
 * - B₀ = 1 (the empty set has one partition)
 * - B₁ = 1 ({1})
 * - B₂ = 2 ({ {1,2}, { {1},{2} } })
 * - B₃ = 5
 *
 * Recurrence:
 *   B₀ = 1
 *   B_{n+1} = Σ_{k=0}^{n} (n choose k) · B_k
 *
 * First few terms:
 *   1, 1, 2, 5, 15, 52, 203, 877, ...
 *
 * Related:
 * - Stirling numbers of the second kind: Bₙ = Σₖ S(n, k)
 * - Exponential generating function: e^{eˣ - 1}
 */
object BellNumbers : Recurrence<BigInteger> by NonlinearRecurrence(
    initial = listOf(BigInteger.ONE),
    next = { terms ->
        val n = terms.lastIndex
        var acc = BigInteger.ZERO
        for (k in 0..n) acc += Binomial(n, k) * terms[k]
        acc
    }
)