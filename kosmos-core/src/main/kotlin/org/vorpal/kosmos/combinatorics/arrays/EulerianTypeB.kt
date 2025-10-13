package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.combinatorics.recurrence.CachedBivariateArray
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Eulerian numbers of type B** B(n, k):
 * count signed permutations of order n with exactly k descents.
 *
 * Recurrence:
 *   B(0, 0) = 1
 *   B(n, k) = (2n − 2k + 1) * B(n − 1, k − 1) + (2k + 1) * B(n − 1, k)
 * valid for 0 ≤ k ≤ n.
 *
 * Closed form:
 *   B(n, k) = Σ_{j=0..k} (−1)^j * C(n+1, j) * (2(k−j)+1)^n
 *
 * First rows (n = 0..5):
 *   1
 *   1 1
 *   1 6 1
 *   1 23 23 1
 *   1 76 230 76 1
 *   1 237 1682 1682 237 1
 *
 * OEIS A060187
 */
object EulerianTypeB : CachedBivariateArray() {

    override fun recursiveCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0..n -> BigInteger.ZERO
            else -> {
                val a = BigInteger.valueOf(2L * n - 2L * k + 1L) * invoke(n - 1, k - 1)
                val b = BigInteger.valueOf(2L * k + 1L) * invoke(n - 1, k)
                a + b
            }
        }

    override fun closedFormCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0..n -> BigInteger.ZERO
            else ->
                (0..k).fold(BigInteger.ZERO) { acc, j ->
                    acc + bigIntSgn(j) * Binomial(n + 1, j) * BigInteger.valueOf(2L * (k - j) + 1L).pow(n)
                }
        }
}