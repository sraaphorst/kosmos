package org.vorpal.kosmos.frameworks.sequence

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

/**
 * Recurrences are sequences that have a recursive definition, and possibly a
 * closed form definition. When treated as sequences, they will rely on the recursive
 * definition.
 */
interface Recurrence<T> : Sequence<T> {
    /**
     * Convenience method to access the nth term of the recurrence.
     */
    operator fun invoke(n: Int): T = recursiveTerm(n)

    /**
     * Generate the nth term of the sequence recursively.
     */
    fun recursiveTerm(n: Int): T

    override fun iterator(): Iterator<T> =
        generateSequence(0) { it + 1 }.map(::invoke).iterator()
}

/**
 * Automatically inherits need to implement recursiveTerm.
 * Has ability to clear cache.
 */
interface CachedRecurrence<T> : Recurrence<T> {
    fun clearRecurrenceCache()
}

/**
 * A recurrence that caches the values it generates recursively.
 * If this class is subclassed or implemented, recursiveCalculator must dictate how to calculate
 * any term in this sequence.
 */
abstract class CachedRecurrenceImplementation<T> : CachedRecurrence<T> {
    private val recurrenceCache = ConcurrentHashMap<Int, T>()

    /**
     * Uses the recursive calculator to - if necessary - calculate the nth term of this
     * recurrence and cache it. The term is then returned.
     */
    final override fun recursiveTerm(n: Int): T {
        recurrenceCache[n]?.let { return it }
        val result = recursiveCalculator(n)
        recurrenceCache[n] = result
        return result
    }

    /**
     * An abstract method that recursively calculates the nth term of this recurrence
     * using recursion, which is cached by the recursiveTerm method.
     * This must be implemented in concrete subclasses of this class.
     */
    protected abstract fun recursiveCalculator(n: Int): T

    /**
     * Clear out the cache used for storing recurrences.
     */
    override fun clearRecurrenceCache() = recurrenceCache.clear()
}


/**
 * A convenience cached linear recurrence where the previous k values are used to calculate the nth value.
 * We must supply:
 * - At minimum k initial values
 * - A list of selectors (must all be negative) of the indices of the previous values to use in the recurrence.
 *   Note that the maximum absolute value must be at most the size of the initialValues.
 * - A list of k coefficients of some type S to serve as the linear multipliers of the previous k terms
 * - The equivalent of a zero in the values of the sequence.
 * - An action of a coefficient S by a sequence value T.
 * - A binary operation to combine two values of the sequence.
 * In this way, a sequence can be seen as an R-Module T being acted on by S.
 */
abstract class CachedLinearRecurrenceImplementation<T, S>(
    val initialValues: List<T>,
    val selectors: List<Int>,
    val coefficients: List<S>,
    val zero: T,
    val multiply: Action<S, T>, // defines an apply(S, T) -> T
    val add: BinOp<T>           // defines a combine(T, T) -> T
) : CachedRecurrenceImplementation<T>() {
    protected val windowSize: Int = initialValues.size

    init {
        require(selectors.isNotEmpty()) { "Linear recurrence requires recurrence selectors." }
        val maxLookback = selectors.maxOf { it.absoluteValue }
        require(selectors.all { it < 0 }) { "All selectors must be negative." }
        require(maxLookback <= windowSize)
        { "Linear recurrence requires at least $maxLookback initial values, but only $windowSize provided." }
        require(coefficients.size == selectors.size)
        { "Size of coefficient list ${coefficients.size} must be equal to size of selector list." }
    }

    override fun recursiveCalculator(n: Int): T = when {
        n < windowSize -> initialValues[n]
        else -> selectors.indices.fold(zero) { acc, i ->
            val coefficient = coefficients[i]
            val termIdx = n + selectors[i]
            val prevTerm = this(termIdx)
            add.combine(acc, multiply.apply(coefficient, prevTerm))
        }
    }
}
