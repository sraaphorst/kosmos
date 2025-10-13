package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.array.CachedBivariateArray
import java.math.BigInteger

/**
 * **Narayana numbers** N(n, k) — a refinement of the Catalan numbers
 * forming the **Narayana triangle**.
 *
 * The Narayana numbers count several combinatorially equivalent objects, including:
 *
 * - The number of **Dyck paths** of semilength *n* with exactly *k* peaks.
 * - The number of **noncrossing partitions** of an *n*-element set with *k* blocks.
 * - The number of **rooted plane trees** with *n* edges and *k* leaves.
 *
 * ---
 *
 * ### Definition
 *
 * The Narayana numbers satisfy the recurrence:
 *
 * ```
 * N(n, k) = (n − k + 1)·N(n−1, k−1) + (k + 1)·N(n−1, k)
 * ```
 *
 * with boundary conditions:
 *
 * ```
 * N(1, 1) = 1
 * N(n, k) = 0  for k < 1 or k > n
 * ```
 *
 * ---
 *
 * ### Closed form
 *
 * The closed form is given by:
 *
 * ```
 * N(n, k) = (1 / n) · C(n, k) · C(n, k − 1)
 * ```
 *
 * where C(a, b) = Binomial(a, b).
 *
 * ---
 *
 * ### Relationship to Catalan numbers
 *
 * The *Catalan numbers* are obtained as row sums of the Narayana triangle:
 *
 * ```
 * Cₙ = Σₖ₌₁ⁿ N(n, k)
 * ```
 *
 * ---
 *
 * ### Example
 *
 * The first few rows of the Narayana triangle:
 *
 * ```
 * n\k│  1   2   3   4   5
 * ───┼────────────────────
 *  1 │  1
 *  2 │  1   1
 *  3 │  1   3   1
 *  4 │  1   6   6   1
 *  5 │  1  10  20  10   1
 * ```
 *
 * Each row sums to the corresponding Catalan number:
 * 1, 2, 5, 14, 42, ...
 *
 * ---
 *
 * ### References
 *
 * - OEIS [A001263](https://oeis.org/A001263) — Narayana numbers.
 * - R. Narayana, *“A Note on the Distribution of Terms in the Product (1 + x)(1 + x + x²)...(1 + x + ⋯ + xⁿ)”*,
 *   *J. Indian Math. Soc.* 15 (1951), 95–96.
 *
 * @see org.vorpal.kosmos.combinatorics.sequences.Catalan
 * @see org.vorpal.kosmos.combinatorics.Binomial
 */
object Narayana : CachedBivariateArray() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger = when {
        n <= 0 || k <= 0 || k > n -> BigInteger.ZERO
        n == 1 -> BigInteger.ONE
        else -> (BigInteger.valueOf((n - k + 1L)) * invoke(n - 1, k - 1) +
                BigInteger.valueOf((k + 1L)) * invoke(n - 1, k))
    }
    override fun closedFormCalculator(n: Int, k: Int): BigInteger =
        Binomial(n, k) * Binomial(n, k - 1) / n.toBigInteger()
}