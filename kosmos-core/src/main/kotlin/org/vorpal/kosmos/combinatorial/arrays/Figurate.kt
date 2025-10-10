package org.vorpal.kosmos.combinatorial.arrays

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.BivariateClosedForm
import org.vorpal.kosmos.memoization.memoize
import org.vorpal.kosmos.memoization.recursiveMemoize2

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
object Figurate : BivariateRecurrence<BigInteger>, BivariateClosedForm<BigInteger> {

    private val recursiveCache = recursiveMemoize2<Int, Int, BigInteger> { self, s, n ->
        when {
            s < 3 || n < 1 -> BigInteger.ZERO
            n == 1 -> BigInteger.ONE
            else -> self(s, n - 1) + BigInteger.valueOf((s - 2L) * (n - 1L) + 1L)
        }
    }

    override fun invoke(s: Int, n: Int): BigInteger = recursiveCache(s, n)

    private val closedFormCache = memoize<Int, Int, BigInteger> { s, n ->
        if (s < 3 || n < 1) BigInteger.ZERO
        else BigInteger.valueOf((s - 2L) * n * n - (s - 4L) * n).divide(BigInteger.TWO)
    }

    override fun closedForm(s: Int, n: Int): BigInteger = closedFormCache(s, n)
}