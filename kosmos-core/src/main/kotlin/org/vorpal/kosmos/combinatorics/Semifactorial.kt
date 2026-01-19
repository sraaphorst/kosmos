package org.vorpal.kosmos.combinatorics

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Semifactorial (Double Factorial) numbers** n!!:
 *
 * The product of all integers from n down to 1 that have the same parity as n.
 *
 * fac2(n) = n!! = n · (n−2)!!
 *
 * Base cases:
 * - 0!! = 1
 * - 1!! = 1
 *
 * OEIS A006882
 */
object Semifactorial :
    CachedRecurrence<BigInteger> by SemifactorialRecurrence

private object SemifactorialRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0 -> error("Semifactorial is undefined for negative values: $n")
        n == 0 || n == 1 -> BigInteger.ONE
        else -> n.toBigInteger() * this(n - 2)
    }
}