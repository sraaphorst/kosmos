package org.vorpal.kosmos.combinatorial.arrays

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence

/**
 * **Eulerian numbers of the second kind** Ā(n,k):
 * count the number of permutations of a multiset of size *n*
 * with exactly *k* ascents.
 *
 * Recurrence:
 * ```
 * Ā(0,0) = 1
 * Ā(n,k) = (k + 1) * Ā(n - 1, k)
 *        + (2n - 1 - k) * Ā(n - 1, k - 1)
 * ```
 * with Ā(n,k) = 0 for k < 0 or k ≥ n.
 *
 * First few rows:
 * ```
 * n=0: 1
 * n=1: 1
 * n=2: 1 2
 * n=3: 1 8 6
 * n=4: 1 22 58 24
 * n=5: 1 52 328 444 120
 * ```
 * OEIS A008517
 */
object EulerianSecond : BivariateRecurrence<BigInteger> {
    private val cache = HashMap<Pair<Int, Int>, BigInteger>()

    /** We have to be careful here to avoid recursive memoization. */
    override fun invoke(n: Int, k: Int): BigInteger =
        cache[n to k] ?: run {
            val res = when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0..<n -> BigInteger.ZERO
                else -> {
                    val term1 = BigInteger.valueOf((k + 1).toLong()) * invoke(n - 1, k)
                    val term2 = BigInteger.valueOf((2L * n - 1L - k)) * invoke(n - 1, k - 1)
                    term1 + term2
                }
            }
            cache[n to k] = res
            res
        }
}