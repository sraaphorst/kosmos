package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * **Type B Eulerian numbers** B(n, k):
 * count signed permutations of {±1,…,±n} with exactly *k* descents.
 *
 * Recurrence:
 * ```
 * B(0,0)=1
 * B(n,k)=(2n−1−k)·B(n−1,k−1)+(k+1)·B(n−1,k)
 * ```
 *
 * OEIS A060187
 */
object EulerianTypeB : BivariateRecurrence<BigInteger> {

    private val cache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        return cache.getOrPut(key) {
            when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0 until n -> BigInteger.ZERO
                else -> {
                    val term1 = BigInteger.valueOf((2L * n - 1L - k)) * invoke(n - 1, k - 1)
                    val term2 = BigInteger.valueOf((k + 1L)) * invoke(n - 1, k)
                    term1 + term2
                }
            }
        }
    }
}