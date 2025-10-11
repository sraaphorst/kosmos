package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * A cached univariate sequence defined by a fixed-size sliding recurrence window.
 *
 * Examples: Fibonacci, Lucas, Tribonacci, Partition, etc.
 */
abstract class CachedWindowedSequence : WindowedRecurrence {
    private val recursiveCache = createCache()

    /** Computes the next term recursively (may depend on `window` previous values). */
    protected abstract val recursiveCalculator: (Int) -> BigInteger

    override fun invoke(n: Int): BigInteger {
        require(n >= 0) { "Sequence index must be non-negative: $n" }

        // Return cached or base case value if possible
        recursiveCache[n]?.let { return it }
        if (n < initial.size) return initial[n]

        // Otherwise compute, store, and return
        val result = recursiveCalculator(n)
        recursiveCache[n] = result
        return result
    }

    /** Provides an infinite iterator over successive terms a₀, a₁, a₂, … */
    override fun iterator(): Iterator<BigInteger> = object : Iterator<BigInteger> {
        private var n = 0
        override fun hasNext() = true
        override fun next(): BigInteger = invoke(n++)
    }

    /** Clears all cached results. */
    fun clear() = recursiveCache.clear()
}