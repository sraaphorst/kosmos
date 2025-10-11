package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.recurrence.CachedBivariateArray
import java.math.BigInteger

/**
 * **Eulerian numbers of the second kind** ⟨⟨n, k⟩⟩ — forming the **Eulerian triangle of the second kind**.
 *
 * These numbers enumerate several combinatorial structures, including:
 *
 * - The number of permutations of the **multiset** {1, 1, 2, 2, …, n, n} having exactly *k* ascents.
 * - The number of “Stirling permutations” of order *n* with *k* ascents.
 * - The coefficients connecting powers and rising factorials:
 *
 *     xⁿ = Σₖ₌₀ⁿ ⟨⟨n, k⟩⟩ (x)ₖ
 *
 * where (x)ₖ = x(x+1)…(x+k−1) denotes the **rising factorial**.
 *
 * ---
 *
 * ### Definition
 *
 * The Eulerian numbers of the second kind are defined for:
 *
 * ```
 * n ≥ 1,  0 ≤ k < n
 * ```
 *
 * and satisfy the recurrence:
 *
 * ```
 * ⟨⟨n, k⟩⟩ = (k + 1) · ⟨⟨n−1, k⟩⟩ + (2n − k − 1) · ⟨⟨n−1, k−1⟩⟩
 * ```
 *
 * with boundary condition:
 *
 * ```
 * ⟨⟨1, 0⟩⟩ = 1
 * ```
 *
 * and zero otherwise when *k* < 0 or *k* ≥ *n*.
 *
 * ---
 *
 * ### Closed form
 *
 * The closed form (Comtet, *Advanced Combinatorics*, §3.6) is:
 *
 * ```
 * ⟨⟨n, k⟩⟩ = Σⱼ₌₀ᵏ (–1)^{k−j} · C(2n+1, k−j) · S(n+j+1, j+1)
 * ```
 *
 * where:
 * - `C(a, b)` = binomial coefficient,
 * - `S(a, b)` = Stirling number of the second kind.
 *
 * ---
 *
 * ### Example
 *
 * The first few rows (n = 1..5) are:
 *
 * ```
 * n\k│  0    1    2    3    4
 * ───┼────────────────────────
 *  1 │  1
 *  2 │  1    2
 *  3 │  1    8    6
 *  4 │  1   22   58   24
 *  5 │  1   52  328  444  120
 * ```
 *
 * ---
 *
 * ### Relationships
 *
 * - Related to the [Eulerian][Eulerian] triangle (first kind) via
 *   transformations involving Stirling numbers.
 * - Row sums yield the **number of Stirling permutations** of order *n*:
 *   Σₖ⟨⟨n, k⟩⟩ = (2n−1)!! = (2n−1)(2n−3)…(3)(1).
 * - Columns correspond to polynomial sequences in *n* of degree *2k*.
 *
 * ---
 *
 * ### References
 * - L. Comtet, *Advanced Combinatorics* (1974), §3.6.
 * - J. Riordan, *Combinatorial Identities* (1968), §4.5.
 * - OEIS [A008517](https://oeis.org/A008517) — Eulerian numbers of the second kind.
 *
 * ---
 *
 * @property n integer row index (n ≥ 1)
 * @property k integer column index (0 ≤ k < n)
 * @see Eulerian
 * @see StirlingSecond
 * @see Binomial
 */
object EulerianSecond : CachedBivariateArray() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger = when {
        n == 0 && k == 0 -> BigInteger.ONE
        n == 0 || k < 0 || k >= n -> BigInteger.ZERO
        else -> {
            val a = BigInteger.valueOf((k + 1L)) * invoke(n - 1, k)
            val b = BigInteger.valueOf((2L * n - k - 1L)) * invoke(n - 1, k - 1)
            a + b
        }
    }

    override fun closedFormCalculator(n: Int, k: Int): BigInteger = when {
        n == 0 && k == 0 -> BigInteger.ONE
        n == 0 || k < 0 || k >= n -> BigInteger.ZERO
        else -> (0..k).fold(BigInteger.ZERO) { acc, j ->
            val sign = if ((k - j) % 2 == 0) BigInteger.ONE else BigInteger.valueOf(-1)
            acc + sign * Binomial(2 * n + 1, k - j) * StirlingSecond(n + j + 1, j + 1)
        }
    }
}
