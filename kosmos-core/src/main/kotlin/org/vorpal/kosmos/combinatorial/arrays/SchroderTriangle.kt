package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import org.vorpal.kosmos.memoization.memoize
import java.math.BigInteger

/**
 * The Schröder triangle is a triangle associated with the [Schroder][org.vorpal.kosmos.combinatorial.sequences.Schroder] numbers.
 *
 */
object SchroderTriangle : BivariateRecurrence<BigInteger> {
    private val cache = memoize<Int, Int, BigInteger> { n, k ->
        when {
            k == 0 -> BigInteger.ONE
            k > n  -> BigInteger.ZERO
            else   -> invoke(n, k - 1) + invoke(n - 1, k - 1) + invoke(n - 1, k)
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = cache(n, k)
}