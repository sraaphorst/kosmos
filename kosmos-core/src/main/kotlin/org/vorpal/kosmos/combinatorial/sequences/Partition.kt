package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedNonlinearSequence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Partition numbers** p(n):
 * number of ways to express n as a sum of positive integers,
 * disregarding order.
 *
 * Recurrence:
 * ```
 * p(0) = 1
 * p(n) = Σₖ (-1)^(k+1) [ p(n - k(3k-1)/2) + p(n - k(3k+1)/2) ]
 * ```
 *
 * OEIS A000041
 */
object Partition : CachedNonlinearSequence(
    initial = listOf(BigInteger.ONE),
    next = { prefix ->
        val n = prefix.size
        var sum = BigInteger.ZERO
        var k = 1
        while (true) {
            val pent1 = n - k * (3 * k - 1) / 2
            val pent2 = n - k * (3 * k + 1) / 2
            if (pent1 < 0 && pent2 < 0) break
            val sign = bigIntSgn(k + 1)
            if (pent1 >= 0) sum += sign * prefix[pent1]
            if (pent2 >= 0) sum += sign * prefix[pent2]
            k++
        }
        sum
    }
)
