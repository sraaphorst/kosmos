package org.vorpal.kosmos.combinatorial

import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.combinatorial.recurrence.WindowedRecurrence
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * **Factorial numbers** n!:
 * the product of all positive integers up to n.
 *
 * Recurrence:
 * ```
 * 0! = 1
 * n! = n × (n−1)!
 * ```
 *
 * OEIS A000142
 */
object Factorial : WindowedRecurrence<BigInteger> {
    override val initial: List<BigInteger> = listOf(BigInteger.ONE)

    /** Thread-safe cache for computed values. */
    private val cache = ConcurrentHashMap<Int, BigInteger>().apply {
        put(0, BigInteger.ONE)
        put(1, BigInteger.ONE)
    }

    /**
     * Computes n! recursively with memoization.
     *
     * @param n the non-negative integer
     * @return n! as a BigInteger
     */
    operator fun invoke(n: Int): BigInteger {
        require(n >= 0) { "Factorial is undefined for negative n: $n" }
        cache[n]?.let { return it }

        val result = when (n) {
            0, 1 -> BigInteger.ONE
            else -> BigInteger.valueOf(n.toLong()) * invoke(n - 1)
        }

        cache[n] = result
        return result
    }

    /** Infinite iterator of factorial numbers: 0!, 1!, 2!, … */
    override fun iterator(): Iterator<BigInteger> = object : Iterator<BigInteger> {
        private var n = 0
        override fun hasNext(): Boolean = true
        override fun next(): BigInteger = invoke(n++)
    }
}