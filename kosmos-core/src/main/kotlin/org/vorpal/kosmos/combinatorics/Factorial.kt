package org.vorpal.kosmos.combinatorics

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Factorial numbers** n!:
 *
 * fac(0) = 1, and fac(n) = n · fac(n−1) for n ≥ 1.
 *
 * OEIS A000142
 */
object Factorial :
        CachedRecurrence<BigInteger> by FactorialRecurrence

private object FactorialRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0  -> error("Factorial is undefined for negative values: $n")
        n == 0 -> BigInteger.ONE
        else -> n.toBigInteger() * this(n-1)
    }
}
