package org.vorpal.kosmos.combinatorics.arrays

import org.vorpal.kosmos.frameworks.array.CachedBivariateRecurrence
import java.math.BigInteger

/**
 * Represents the **Stirling numbers of the first kind** s(n, k).
 *
 * These count the number of permutations of **n** elements that have exactly
 * **k** disjoint cycles in their cycle decomposition.
 *
 * By convention, s(n, k) may be **signed** or **unsigned**:
 * - The signed Stirling numbers are denoted s(n, k) and alternate in sign.
 * - The unsigned Stirling numbers are denoted |s(n, k)| and are always non-negative.
 *
 * ### Definition
 * s(n, k) = number of permutations of n elements with exactly k cycles.
 *
 * ### Recurrence relation
 * ```
 * s(n, k) = s(n - 1, k - 1) - (n - 1) * s(n - 1, k)
 * ```
 * where:
 * - `s(n - 1, k - 1)` accounts for introducing a new singleton cycle containing element n.
 * - `(n - 1) * s(n - 1, k)` accounts for inserting element n into an existing cycle
 *   of a permutation of n - 1 elements (hence the subtraction for sign consistency).
 *
 * ### Boundary conditions
 * ```
 * s(0, 0) = 1
 * s(n, 0) = 0  for n > 0
 * s(0, k) = 0  for k > 0
 * s(n, n) = 1
 * ```
 *
 * ### Example
 * ```
 * s(4, 2) = 11
 * ```
 * (There are 11 permutations of 4 elements with exactly 2 cycles.)
 *
 * ### Relationship to falling factorials
 * Stirling numbers of the first kind are the coefficients in the expansion:
 * ```
 * x(x - 1)(x - 2)...(x - n + 1) = Σ_{k=0}^{n} s(n, k) * x^k
 * ```
 *
 * ### Sign convention
 * The signed and unsigned forms are related by:
 * ```
 * s(n, k) = (-1)^{n - k} * |s(n, k)|
 * ```
 *
 * Note that there is no closed form for Stirling numbers of the first kind.
 */
object StirlingFirst : CachedBivariateRecurrence() {
    override fun recursiveCalculator(n: Int, k: Int): BigInteger =
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0..n -> BigInteger.ZERO
            k == 0 -> BigInteger.ZERO
            // TODO: Change - to + for unsigned
            else -> invoke(n - 1, k - 1) - (n - 1).toBigInteger() * invoke(n - 1, k)
        }
}
