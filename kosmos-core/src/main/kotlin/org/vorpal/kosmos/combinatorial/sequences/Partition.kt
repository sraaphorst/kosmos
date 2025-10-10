package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.memoization.memoize

/**
 * **Integer partition numbers** `p(n)` — the number of ways of writing *n*
 * as a sum of positive integers, ignoring order.
 *
 * Defined by Euler's pentagonal number theorem:
 *
 * ```
 * p(0) = 1
 * p(n) = Σ_{k=1..∞} (-1)^(k+1) [p(n - k(3k-1)/2) + p(n - k(3k+1)/2)]
 * ```
 *
 * with `p(m) = 0` for all `m < 0`.
 *
 * ---
 *
 * Example values:
 * ```
 * n:  0  1  2  3  4  5  6  7  8  9  10
 * p:  1  1  2  3  5  7 11 15 22 30 42
 * ```
 *
 * OEIS A000041
 *
 * ---
 *
 * - See [Bell]
 * - See [StirlingSecond][org.vorpal.kosmos.combinatorial.arrays.StirlingSecond]
 * - See [Catalan]
 */
object Partition : Recurrence<BigInteger> {
    override val initial: List<BigInteger> = listOf(BigInteger.ONE)

    // Memoized recursive partition function
    private val cache = memoize<Int, BigInteger> { n ->
        when {
            n < 0 -> BigInteger.ZERO
            n == 0 -> BigInteger.ONE
            else -> {
                var sum = BigInteger.ZERO
                var k = 1
                while (true) {
                    val pent1 = n - k * (3 * k - 1) / 2
                    val pent2 = n - k * (3 * k + 1) / 2
                    if (pent1 < 0 && pent2 < 0) break
                    val sign = if (k % 2 == 1) BigInteger.ONE else BigInteger.valueOf(-1)
                    if (pent1 >= 0) sum += sign * invoke(pent1)
                    if (pent2 >= 0) sum += sign * invoke(pent2)
                    k++
                }
                sum
            }
        }
    }

    /** Compute the partition number p(n). */
    operator fun invoke(n: Int): BigInteger = cache(n)

    override fun iterator(): Iterator<BigInteger> =
        generateSequence(0) { it + 1 }.map(::invoke).iterator()
}