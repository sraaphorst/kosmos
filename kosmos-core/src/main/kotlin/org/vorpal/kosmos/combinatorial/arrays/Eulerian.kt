package org.vorpal.kosmos.combinatorial.arrays

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence

/**
 * **Eulerian numbers** A(n, k):
 * the number of permutations of {1,…,n} with exactly *k* ascents.
 *
 * Recurrence:
 * ```
 * A(0, 0) = 1
 * A(n, k) = (n - k) * A(n - 1, k - 1) + (k + 1) * A(n - 1, k)
 * ```
 *
 * Valid for 0 ≤ k < n.
 *
 * First few rows:
 * ```
 * n=0: 1
 * n=1: 1
 * n=2: 1 1
 * n=3: 1 4 1
 * n=4: 1 11 11 1
 * n=5: 1 26 66 26 1
 * ```
 *
 * OEIS A173018
 */
object Eulerian : BivariateRecurrence<BigInteger> {
    private val cache = HashMap<Pair<Int, Int>, BigInteger>()

    /**
     * This must be treated specially to avoid IllegalStateExceptions from recursive memoization.
     * This is slightly less efficient, but still calculates quickly enough.
     */
    override fun invoke(n: Int, k: Int): BigInteger =
        cache[n to k] ?: run {
            val res = when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0 until n  -> BigInteger.ZERO
                else -> {
                    val a = BigInteger.valueOf((n - k).toLong()) * invoke(n - 1, k - 1)
                    val b = BigInteger.valueOf((k + 1).toLong()) * invoke(n - 1, k)
                    a + b
                }
            }
            cache[n to k] = res
            res
        }
}