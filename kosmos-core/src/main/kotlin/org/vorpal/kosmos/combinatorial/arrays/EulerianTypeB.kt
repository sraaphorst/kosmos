package org.vorpal.kosmos.combinatorial.arrays

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence

/**
 * **Type B Eulerian numbers** B(n,k):
 * the number of signed permutations of {±1,…,±n} with exactly *k* ascents.
 *
 * Recurrence:
 * ```
 * B(0,0) = 1
 * B(n,k) = (2n - 2k + 1) * B(n-1, k-1)
 *        + (2k + 1) * B(n-1, k)
 * ```
 * with B(n,k) = 0 for k < 0 or k > n.
 *
 * First few rows:
 * ```
 * n=0: 1
 * n=1: 1 1
 * n=2: 1 6 1
 * n=3: 1 23 23 1
 * n=4: 1 76 230 76 1
 * ```
 * OEIS A060187
 */
object EulerianTypeB : BivariateRecurrence<BigInteger> {
    private val cache = HashMap<Pair<Int, Int>, BigInteger>()

    /** We have to be careful here to avoid recursive memoization. */
    override fun invoke(n: Int, k: Int): BigInteger =
        cache[n to k] ?: run {
            val res = when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0..n -> BigInteger.ZERO
                else -> {
                    val term1 = BigInteger.valueOf((2L * n - 2L * k + 1L)) * invoke(n - 1, k - 1)
                    val term2 = BigInteger.valueOf((2L * k + 1L)) * invoke(n - 1, k)
                    term1 + term2
                }
            }
            cache[n to k] = res
            res
        }
}