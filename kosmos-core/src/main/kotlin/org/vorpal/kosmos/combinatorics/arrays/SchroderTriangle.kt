package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.recurrence.CachedBivariateRecurrence
import java.math.BigInteger

/**
 * The Schröder triangle is a triangle associated with the [Schroder][org.vorpal.kosmos.combinatorics.sequences.Schroder] numbers.
 *
 */
object SchroderTriangle : CachedBivariateRecurrence() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger =
        when {
            k == 0 -> BigInteger.ONE
            k > n  -> BigInteger.ZERO
            else   -> invoke(n, k - 1) + invoke(n - 1, k - 1) + invoke(n - 1, k)
        }
}