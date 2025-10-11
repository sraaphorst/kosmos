package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * Cached nonlinear univariate recurrence:
 * given the already-computed prefix [a0, a1, ..., a_{n-1}],
 * compute a_n = next(prefix).
 *
 * The [initial] list provides a0..a_{m-1}. For n < initial.size, values come from [initial].
 * For n >= initial.size, we build a prefix [a0..a_{n-1}] (using cached invoke)
 * and pass it to [next] to compute a_n.
 */
open class CachedNonlinearRecurrence(
    override val initial: List<BigInteger>,
    private val next: (prefix: List<BigInteger>) -> BigInteger
) : CachedWindowedSequence() {

    init {
        require(initial.isNotEmpty()) { "initial cannot be empty" }
    }

    // Nonlinear recurrences generally have no fixed finite window.
    override val window: Int = Int.MAX_VALUE

    override val recursiveCalculator = { n: Int ->
        // Build a snapshot prefix [a0..a_{n-1}] from the cache.
        // Each invoke(i) is O(1) thanks to caching; the snapshot build is O(n).
        val prefix = (0 until n).map(::invoke)
        next(prefix)
    }
}
