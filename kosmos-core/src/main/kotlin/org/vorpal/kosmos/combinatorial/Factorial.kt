package org.vorpal.kosmos.combinatorial

import org.vorpal.kosmos.combinatorial.recurrence.CachedRecursiveSequence
import java.math.BigInteger

/**
 * **Factorial numbers** n!:
 *
 * fac(0) = 1, and fac(n) = n · fac(n−1) for n ≥ 1.
 *
 * OEIS A000142
 */
object Factorial : CachedRecursiveSequence() {
    override val initial = listOf(BigInteger.ONE)
    override fun recursiveCalculator(n: Int): BigInteger =
        when {
            n < 0  -> error("Factorial is undefined for negative n: $n")
            n == 0 -> BigInteger.ONE          // also covered by initial
            else   -> BigInteger.valueOf(n.toLong()) * this(n - 1)
        }
}