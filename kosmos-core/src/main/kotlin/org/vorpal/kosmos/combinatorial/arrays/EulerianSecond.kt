package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * **Eulerian numbers of the second kind** ⟨n,k⟩:
 * number of permutations of n elements with *k* ascending runs.
 *
 * Recurrence:
 * ```
 * ⟨0,0⟩=1
 * ⟨n,k⟩=(k+1)·⟨n−1,k⟩+(n−k)·⟨n−1,k−1⟩
 * ```
 *
 * OEIS A008517
 */
object EulerianSecond : BivariateRecurrence<BigInteger> {

    private val cache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        return cache.getOrPut(key) {
            when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0 until n -> BigInteger.ZERO
                else -> {
                    val a = BigInteger.valueOf((k + 1L)) * invoke(n - 1, k)
                    val b = BigInteger.valueOf((n - k).toLong()) * invoke(n - 1, k - 1)
                    a + b
                }
            }
        }
    }
}