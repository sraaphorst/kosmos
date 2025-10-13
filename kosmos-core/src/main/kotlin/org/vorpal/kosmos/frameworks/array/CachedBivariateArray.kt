package org.vorpal.kosmos.frameworks.array

/**
 * A bivariate array whose terms:
 * 1. Are recursively computable.
 * 2. Have a closed form.
 */
abstract class CachedBivariateArray<T>: CachedBivariateRecurrence<T>(), BivariateClosedForm<T> {
    private val closedFormCache = createCache()

    /** Function that definees how to closed-form compute a term t(n, k). */
    abstract fun closedFormCalculator(n: Int, k: Int): T

    /** Cached closed-form invocation. */
    final override fun closedForm(n: Int, k: Int): T {
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
