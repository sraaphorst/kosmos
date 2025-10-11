package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * Abstract base class for recursively-defined sequences with caching.
 *
 * Provides memoization and [Sequence] iteration over the natural numbers.
 */
abstract class CachedRecursiveSequence : CachedSequence {

    protected val recursiveCache = createCache()

    override fun invoke(n: Int): BigInteger {
        recursiveCache[n]?.let { return it }
        val value = recursiveCalculator(n)
        recursiveCache[n] = value
        return value
    }

    /** Compute the nth term (to be implemented by subclasses). */
    protected abstract fun recursiveCalculator(n: Int): BigInteger

    /** Infinite iterator over all terms a₀, a₁, a₂, … */
    override fun iterator(): Iterator<BigInteger> =
        generateSequence(0) { it + 1 }.map(::invoke).iterator()

    override fun clear() = recursiveCache.clear()
}