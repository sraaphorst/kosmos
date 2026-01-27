package org.vorpal.kosmos.combinatorics.arrays

import java.math.BigInteger
import org.vorpal.kosmos.frameworks.array.CachedBivariateArray

/**
 * **Figurate (polygonal) numbers** P(s, n):
 * the *n*th polygonal number with *s* sides.
 *
 * Closed form:
 * ```
 * P(s, n) = ((s - 2) * n^2 - (s - 4) * n) / 2
 * ```
 *
 * Recurrence (over n for fixed s):
 * ```
 * P(s, 1) = 1
 * P(s, n) = P(s, n - 1) + (s - 2)(n - 1) + 1
 * ```
 *
 * Examples:
 * ```
 * s=3 (triangular): 1, 3, 6, 10, 15, 21, ...
 * s=4 (square):     1, 4, 9, 16, 25, 36, ...
 * s=5 (pentagonal): 1, 5, 12, 22, 35, 51, ...
 * s=6 (hexagonal):  1, 6, 15, 28, 45, 66, ...
 * ```
 * OEIS families: A000217 (triangular), A000290 (square), A000326 (pentagonal)
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object Figurate : CachedBivariateArray<BigInteger>() {
    override fun recursiveCalculator(s: Int, n: Int): BigInteger = when {
            s < 3 || n < 1 -> BigInteger.ZERO
            n == 1 -> BigInteger.ONE
            else -> this(s, n - 1) + BigInteger.valueOf((s - 2L) * (n - 1L) + 1L)
        }

    override fun closedFormCalculator(s: Int, n: Int): BigInteger =
        if (s < 3 || n < 1) BigInteger.ZERO
        else {
            val sBig = s.toBigInteger()
            val nBig = n.toBigInteger()
            ((sBig - BigInteger.TWO) * nBig * nBig - (sBig - BigInteger.valueOf(4)) * nBig) / BigInteger.TWO
        }
}
