package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.Factorial
import org.vorpal.kosmos.memoization.memoize
import java.math.BigInteger

/**
 * Represents the **Lah numbers** L(n, k).
 *
 * These count the number of ways to partition a set of **n** elements
 * into **k** non-empty **linearly ordered** subsets (i.e., ordered lists rather than sets).
 *
 * In other words, Lah numbers count the number of ways to arrange n labeled objects
 * into k non-empty ordered blocks, where both the order of blocks and the order
 * of elements within each block matter.
 *
 * ### Definition
 * L(n, k) = number of ways to partition n elements into k ordered subsets.
 *
 * ### Closed form
 * ```
 * L(n, k) = (n! / k!) * (n - 1 choose k - 1)
 * ```
 *
 * ### Recurrence relation
 * ```
 * L(n, k) = L(n - 1, k - 1) + (n + k - 1) * L(n - 1, k)
 * ```
 * where:
 * - `L(n - 1, k - 1)` accounts for starting a new list with element n.
 * - `(n + k - 1) * L(n - 1, k)` accounts for inserting element n into
 *   any position among the existing k lists.
 *
 * ### Boundary conditions
 * ```
 * L(0, 0) = 1
 * L(n, 0) = 0  for n > 0
 * L(0, k) = 0  for k > 0
 * L(n, n) = 1
 * ```
 *
 * ### Example
 * ```
 * L(4, 2) = 36
 * ```
 * meaning there are 36 ways to arrange 4 labeled elements into 2 ordered lists.
 */
object Lah : BivariateRecurrence<BigInteger> {
    private val cache = memoize<Int, Int, BigInteger> { n, k ->
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k == 0 || k > n  -> BigInteger.ZERO
            n == k           -> BigInteger.ONE
            else             -> {
                val term1 = invoke(n - 1, k - 1)
                val factor = BigInteger.valueOf((n + k - 1).toLong())
                val term2 = factor * invoke(n - 1, k)
                term1 + term2
            }
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = cache(n, k)

    /** Optional: closed form if you ever want a direct path. */
    fun closedForm(n: Int, k: Int): BigInteger =
        if (k in 1..n)
            Binomial(n - 1, k - 1) * Factorial(n) / Factorial(k)
        else if (n == 0 && k == 0) BigInteger.ONE else BigInteger.ZERO
}
