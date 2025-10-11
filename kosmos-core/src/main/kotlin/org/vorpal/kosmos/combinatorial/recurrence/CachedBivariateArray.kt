package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * A bivariate array whose terms:
 * 1. Are recursively computable.
 * 2. Have a closed form.
 */
abstract class CachedBivariateArray: CachedBivariateRecurrence(), BivariateClosedForm {
    private val closedFormCache = createCache()

    /** Function that definees how to closed-form compute a term t(n, k). */
    abstract fun closedFormCalculator(n: Int, k: Int): BigInteger

    /** Cached closed-form invocation. */
    final override fun closedForm(n: Int, k: Int): BigInteger {
        val key = n to k
        closedFormCache[key]?.let { return it }
        val result = closedFormCalculator(n, k)
        closedFormCache[key] = result
        return result
    }

    /** Clear any cached values. */
    override fun clear() {
        super.clear()
        closedFormCache.clear()
    }
}
