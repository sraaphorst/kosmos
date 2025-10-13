package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.frameworks.array.CachedBivariateArray
import java.math.BigInteger

/**
 * **Entringer numbers** E(n, k) — the Entringer (Seidel) triangle.
 *
 * E(n, k) counts alternating (up–down) permutations of {1,…,n+1} that start with an ascent
 * and whose first element is exactly k+1.
 *
 * Standard recurrence (Seidel’s rule):
 * ```
 * E(0, 0) = 1
 * E(n, 0) = 0 for n > 0
 * E(n, k) = E(n, k-1) + E(n-1, n-k)   for 1 ≤ k ≤ n
 * E(n, k) = 0                         for k < 0 or k > n
 * ```
 *
 * Row sums give the **Euler zigzag / up–down numbers** A(n):
 *     A(n) = Σ_{k=0}^{n} E(n, k)
 *
 * First rows:
 * n=0: 1
 * n=1: 0 1
 * n=2: 1 1 0
 * n=3: 0 2 2 0
 * n=4: 5 5 3 1 0
 *
 * OEIS:
 * - Entringer triangle: A008281
 * - Row sums (Euler zigzag): A000111
 */
object Entringer : CachedBivariateArray<BigInteger>() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger = when {
        n == 0 && k == 0 -> BigInteger.ONE
        n > 0 && k == 0  -> BigInteger.ZERO
        k !in 0..n -> BigInteger.ZERO
        else             -> this(n, k - 1) + this(n - 1, n - k)
    }

    // No simple closed form used here.
    override fun closedFormCalculator(n: Int, k: Int): BigInteger =
        recursiveCalculator(n, k)
}
