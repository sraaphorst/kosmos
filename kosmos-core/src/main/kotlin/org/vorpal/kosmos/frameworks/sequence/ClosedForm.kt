package org.vorpal.kosmos.frameworks.sequence

import java.util.concurrent.ConcurrentHashMap


/**
 * An interface if a recurrence has a closed form.
 * This should be in addition to it having a recursive form, and not instead of.
 */
interface ClosedForm<T> {
    fun closedForm(n: Int): T
}

interface CachedClosedForm<T> : ClosedForm<T> {
    fun clearClosedFormCache()
}

/**
 * A mixin cached closed form calculator.
 */
abstract class CachedClosedFormImplementation<T> : CachedClosedForm<T> {
    private val closedFormCache = ConcurrentHashMap<Int, T>()

    /**
     * Calculate and cache if necessary the closed form of this recurrence.
     */
    final override fun closedForm(n: Int): T {
        closedFormCache[n]?.let { return it }
        val result = closedFormCalculator(n)
        closedFormCache[n] = result
        return result
    }

    /**
     * Abstract calculator to calculate the closed form of this recurrence for term n.
     */
    abstract fun closedFormCalculator(n: Int): T

    /**
     * Clear out the cache used for storing closed forms.
     */
    override fun clearClosedFormCache() = closedFormCache.clear()
}
