package org.vorpal.kosmos.combinatorics

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import org.vorpal.kosmos.frameworks.array.BivariateRecurrence
import kotlin.math.min

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
object Binomial : BivariateRecurrence<BigInteger> {
    private val cache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override operator fun invoke(n: Int, k: Int): BigInteger {
        if (k !in 0..n) {
            return BigInteger.ZERO
        }

        val kk = min(k, n - k)
        val key = n to kk
        cache[key]?.let { return it }

        val result = when (kk) {
            0 -> BigInteger.ONE
            else -> invoke(n - 1, kk - 1) + invoke(n - 1, kk)
        }

        cache[key] = result
        return result
    }
}