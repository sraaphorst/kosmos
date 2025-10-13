package org.vorpal.kosmos.combinatorics

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import org.vorpal.kosmos.frameworks.array.BivariateRecurrence

/**
 * **Binomial coefficients** C(n,k) = n choose k.
 *
 * Recurrence:
 * ```
 * C(n, 0) = C(n, n) = 1
 * C(n, k) = C(n-1, k-1) + C(n-1, k)
 * ```
 *
 * OEIS A007318
 */
object Binomial : BivariateRecurrence {

    private val cache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override operator fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        cache[key]?.let { return it }

        val result = when (k) {
            !in 0..n -> BigInteger.ZERO
            0, n -> BigInteger.ONE
            else -> invoke(n - 1, k - 1) + invoke(n - 1, k)
        }

        cache[key] = result
        return result
    }
}