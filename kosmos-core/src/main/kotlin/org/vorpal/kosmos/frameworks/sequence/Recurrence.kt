package org.vorpal.kosmos.frameworks.sequence

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import java.util.concurrent.ConcurrentHashMap

/**
 * Recurrences are sequences that have a recursive definition, and possibly a
 * closed form definition. When treated as sequences, they will rely on the recursive
 * definition.
 */
interface Recurrence<T: Any> : Sequence<T> {
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
interface CachedRecurrence<T: Any> : Recurrence<T> {
    fun clearRecurrenceCache()
}

/**
 * A recurrence that caches the values it generates recursively.
 * If this class is subclassed or implemented, recursiveCalculator must dictate how to calculate
 * any term in this sequence.
 */
abstract class CachedRecurrenceImplementation<T: Any> : CachedRecurrence<T> {
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
 * A convenience cached linear recurrence where the previous k values and a scalar term
 * are used to calculate the nth value. Let `S` be the sequence.
 *
 * We must supply:
 * @param initialValues The initial values of the sequence, i.e. `S[i] = initialValues[i]` for `i ∈ initialValues.size`.
 * @param selectors The offset of the selectors to calculate the `n`th term, e.g. `(-1, -2)` indicates that
 *                  `S[n]` is a linear function of `S[n-1]` and `S[n-2]`.
 *                  Note that the values must all be negative,
 *                  and `max(-selectors)` must be at most `initialValues.size`.
 * @param coefficients The coefficients for the terms chosen by the `selectors`.
 * @param constantTerm A constant term used in the linear recurrence, typically 0.
 * @param multiply An action taking a coefficient and applying it to a sequence term.
 *                 Most typically, this would be mapping the coefficient to the type of `T` and multiplying.
 * @param add An action that adds two objects of type `T` together, to combine the terms of the linear relation.
 */
abstract class CachedLinearRecurrenceImplementation<T: Any, S: Any>(
    val initialValues: List<T>,
    val selectors: List<Int>,
    val coefficients: List<S>,
    val constantTerm: T,
    val multiply: Action<S, T>, // defines an apply(S, T) -> T
    val add: BinOp<T> // defines a combine(T, T) -> T
) : CachedRecurrenceImplementation<T>() {
    protected val windowSize: Int = initialValues.size

    init {
        require(selectors.isNotEmpty()) { "Linear recurrence requires recurrence selectors." }
        require(selectors.all { it < 0 }) { "All selectors must be negative." }
        val maxLookback = selectors.maxOf { -it }
        require(maxLookback <= windowSize)
        { "Linear recurrence requires at least $maxLookback initial values, but only $windowSize provided." }
        require(coefficients.size == selectors.size)
        { "Size of coefficient list ${coefficients.size} must be equal to size of selector list." }
    }

    override fun recursiveCalculator(n: Int): T = when {
        n < windowSize -> initialValues[n]
        else -> selectors.indices.fold(constantTerm) { acc, i ->
            val coefficient = coefficients[i]
            val termIdx = n + selectors[i]
            val prevTerm = this(termIdx)
            add(acc, multiply(coefficient, prevTerm))
        }
    }
}
