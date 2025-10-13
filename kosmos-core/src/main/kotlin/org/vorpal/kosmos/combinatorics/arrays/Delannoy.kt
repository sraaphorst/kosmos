package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.combinatorics.recurrence.CachedBivariateArray
import java.math.BigInteger
import kotlin.math.min

/**
 * **Delannoy numbers** D(m, n).
 *
 * Counts the number of lattice paths from `(0, 0)` to `(m, n)`
 * using only the steps:
 *
 * - East `(1, 0)`
 * - North `(0, 1)`
 * - Northeast `(1, 1)`
 *
 * In other words, `D(m, n)` is the number of ways to reach `(m, n)`
 * on a grid if diagonal moves are allowed.
 *
 * ---
 *
 * ### Recurrence
 *
 * ```
 * D(m, n) = D(m−1, n) + D(m, n−1) + D(m−1, n−1)
 * ```
 *
 * with base cases:
 *
 * ```
 * D(0, n) = D(m, 0) = 1
 * ```
 *
 * ---
 *
 * ### Closed form
 *
 * ```
 * D(m, n) = Σₗ₌₀^{min(m,n)} C(m+n−l, m) · C(m, l)
 * ```
 *
 * where `C(a, b)` are binomial coefficients.
 *
 * This form shows that `D(m, n)` is symmetric:
 * `D(m, n) = D(n, m)`.
 *
 * ---
 *
 * ### Special cases
 *
 * - **Central Delannoy numbers**: `D(n, n)`
 *   1, 3, 13, 63, 321, 1683, 8989, … (OEIS [A001850](https://oeis.org/A001850))
 *
 * - **Edge cases**: `D(0, n) = D(m, 0) = 1`
 *
 * ---
 *
 * ### Examples
 *
 * ```
 * D(1, 1) = 3
 * D(2, 2) = 13
 * D(3, 3) = 63
 * D(4, 4) = 321
 * ```
 *
 * ---
 *
 * ### Related sequences
 *
 * - [Pascal] triangle (binomial coefficients)
 * - [Trinomial] triangle
 * - Central Delannoy → counts of rook paths with diagonal steps
 * - Small Delannoy → Dyck-like constrained paths
 *
 * ---
 *
 * ### References
 *
 * - Riordan, *Combinatorial Identities* (1968), §3.7
 * - Comtet, *Advanced Combinatorics* (1974), §3.6
 * - OEIS [A008288](https://oeis.org/A008288) — Delannoy numbers
 * - OEIS [A001850](https://oeis.org/A001850) — Central Delannoy numbers
 */
object Delannoy : CachedBivariateArray() {

    override fun recursiveCalculator(m: Int, n: Int): BigInteger =
        when {
            m < 0 || n < 0 -> BigInteger.ZERO
            m == 0 || n == 0 -> BigInteger.ONE
            else -> invoke(m - 1, n) + invoke(m, n - 1) + invoke(m - 1, n - 1)
        }

    override fun closedFormCalculator(m: Int, n: Int): BigInteger =
        when {
            m < 0 || n < 0 -> BigInteger.ZERO
            m == 0 || n == 0 -> BigInteger.ONE
            else -> (0..min(m, n)).fold(BigInteger.ZERO) { acc, l ->
                acc + Binomial(m + n - l, m) * Binomial(m, l)
            }
        }

    /**
     * Returns a **lazy sequence** of Delannoy numbers for a fixed `m`
     * varying over `n = 0, 1, 2, …`.
     *
     * For example:
     * ```
     * Delannoy.row(3).take(5).toList()
     * → [1, 4, 13, 40, 121]
     * ```
     */
    override fun row(m: Int): Sequence<BigInteger> =
        generateSequence(0) { it + 1 }.map { n -> invoke(m, n) }
}
