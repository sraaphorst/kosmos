package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.core.math.bigIntSgn
import org.vorpal.kosmos.frameworks.array.CachedBivariateArray
import java.math.BigInteger

/**
 * **Eulerian numbers** A(n, k):
 * number of permutations of {1,…,n} with exactly *k* ascents.
 *
 * Recurrence:
 * ```
 * A(0,0)=1
 * A(n,k)=(n−k)·A(n−1,k−1)+(k+1)·A(n−1,k)
 * ```
 *
 * Closed form:
 * ```
 * A(n,k)=Σⱼ₌₀ᵏ(−1)ʲ·C(n+1,j)·(k+1−j)ⁿ
 * ```
 *
 * OEIS A008292
 */
object Eulerian : CachedBivariateArray<BigInteger>() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0 until n -> BigInteger.ZERO
            else -> {
                val a = BigInteger.valueOf((n - k).toLong()) * invoke(n - 1, k - 1)
                val b = BigInteger.valueOf((k + 1).toLong()) * invoke(n - 1, k)
                a + b
            }
        }

    override fun closedFormCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0 until n -> BigInteger.ZERO
            else ->
                (0..k).fold(BigInteger.ZERO) { acc, j ->
                    acc + bigIntSgn(j) * Binomial(n + 1, j) * BigInteger.valueOf((k + 1L - j)).pow(n)
                }
        }
}