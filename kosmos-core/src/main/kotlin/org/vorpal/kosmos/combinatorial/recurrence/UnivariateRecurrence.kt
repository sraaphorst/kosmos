package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Defines a univariate recurrence-based sequence:
 *   a₀, a₁, a₂, ...
 * Examples: Factorial, Fibonacci, Bell numbers, etc.
 */
interface UnivariateRecurrence : Iterable<BigInteger> {
    fun createCache() = ConcurrentHashMap<Int, BigInteger>()

    operator fun invoke(n: Int): BigInteger
}
