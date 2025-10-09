package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.memoization.memoize
import java.math.BigInteger

/**
 * Pascal's triangle: the infinite array of **binomial coefficients**.
 *
 * Each entry satisfies:
 *
 *     C(n, k) = C(n-1, k-1) + C(n-1, k)
 *
 * with base cases:
 *
 *     C(n, 0) = C(n, n) = 1
 *     C(n, k) = 0 for k < 0 or k > n
 *
 * These are the coefficients of the binomial expansion:
 *
 *     (x + y)^n = Σₖ C(n, k) * x^{n-k} * y^k
 *
 * Related:
 * - The binomial function in Combinatorial.
 * - Stirling numbers (partition counts)
 * - Lah numbers (ordered partition counts)
 * - Bell numbers (sum over Stirling)
 *
 * First few rows:
 * ```
 * n=0: 1
 * n=1: 1 1
 * n=2: 1 2 1
 * n=3: 1 3 3 1
 * n=4: 1 4 6 4 1
 * ```
 */
object Pascal : BivariateRecurrence<BigInteger> {
    private val cache = memoize<Int, Int, BigInteger> { n, k ->
        when (k) {
            !in 0..n -> BigInteger.ZERO
            0, n           -> BigInteger.ONE
            else           -> invoke(n - 1, k - 1) + invoke(n - 1, k)
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = cache(n, k)
}
