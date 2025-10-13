package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.array.CachedBivariateArray
import java.math.BigInteger

/**
 * **Trinomial triangle** — coefficients of `(1 + x + x²)ⁿ`.
 *
 * Denoted `T(n, k)`, where:
 *
 * ```
 * (1 + x + x²)ⁿ = Σₖ₌₀^{2n} T(n, k) xᵏ
 * ```
 *
 * ---
 *
 * ### Definition
 *
 * The coefficients satisfy the recurrence:
 *
 * ```
 * T(n, k) = T(n−1, k−2) + T(n−1, k−1) + T(n−1, k)
 * ```
 *
 * with base case:
 *
 * ```
 * T(0, 0) = 1
 * ```
 *
 * and `T(n, k) = 0` for `k < 0` or `k > 2n`.
 *
 * ---
 *
 * ### Closed form
 *
 * ```
 * T(n, k) = Σⱼ₌₀^{⌊k/2⌋} C(n, j) · C(n−j, k−2j)
 * ```
 *
 * where `C(a, b)` are binomial coefficients.
 *
 * ---
 *
 * ### Example
 *
 * First few rows (n = 0..4):
 *
 * ```
 * n=0:                        1
 * n=1:                     1  1  1
 * n=2:                  1  2  3  2  1
 * n=3:               1  3  6  7  6  3  1
 * n=4:            1  4 10 16 19 16 10  4  1
 * ```
 *
 * ---
 *
 * ### Relationships
 *
 * - Generalizes [Pascal]’s triangle (binomial coefficients).
 * - `T(n, n)` gives the **central trinomial coefficients** (OEIS [A002426](https://oeis.org/A002426)).
 * - Related to Motzkin and Delannoy numbers.
 *
 * ---
 *
 * @property n the power (n ≥ 0)
 * @property k the coefficient index (0 ≤ k ≤ 2n)
 */
object Trinomial : CachedBivariateArray<BigInteger>() {

    override fun recursiveCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            n == 0 || k < 0 || k > 2 * n -> BigInteger.ZERO
            else ->
                invoke(n - 1, k - 2) + invoke(n - 1, k - 1) + invoke(n - 1, k)
        }

    override fun closedFormCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            n == 0 || k < 0 || k > 2 * n -> BigInteger.ZERO
            else ->
                (0..(k / 2)).fold(BigInteger.ZERO) { acc, j ->
                    acc + Binomial(n, j) * Binomial(n - j, k - 2 * j)
                }
        }

    /**
     * Returns a **lazy sequence** of trinomial coefficients for the given row `n`.
     *
     * For example:
     * ```
     * Trinomial.sequenceRow(3).toList()
     * → [1, 3, 6, 7, 6, 3, 1]
     * ```
     *
     * The values are computed using the cached recurrence or closed form as needed.
     */
    override fun row(n: Int): Sequence<BigInteger> =
        generateSequence(0) { it + 1 }
            .map { k -> invoke(n, k) }
            .take(2 * n + 1)
}
