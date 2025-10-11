package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * A bivariate array whose terms can be calculated recursively.
 */
abstract class CachedBivariateRecurrence: BivariateRecurrence {
    private val recursiveCache = createCache()

    /** Function that defines how to compute a term t(n, k) recursively. */
    abstract fun recursiveCalculator(n: Int, k: Int): BigInteger

    /** Cached recursive invocation. */
    final override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        recursiveCache[key]?.let { return it }
        val result = recursiveCalculator(n, k)
        recursiveCache[key] = result
        return result
    }

    /** Clear any cached values. */
    open fun clear() = recursiveCache.clear()
}