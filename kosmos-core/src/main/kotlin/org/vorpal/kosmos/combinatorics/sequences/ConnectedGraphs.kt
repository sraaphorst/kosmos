package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Connected labeled graphs** Cₙ — OEIS A001187.
 *
 * Number of connected graphs on n labeled vertices.
 *
 * Recurrence:
 * ```
 * C₀ = 0
 * C₁ = 1
 * Cₙ = 2^{n choose 2} − Σₖ₌₁ⁿ⁻¹ binom(n−1, k−1) · 2^{(n−k)(n−k−1)/2} · Cₖ
 * ```
 *
 * This recurrence comes from the exponential formula
 * linking general and connected labeled graph species.
 *
 * Examples:
 * ```
 * n : 0, 1, 2, 3, 4, 5, 6
 * C : 0, 1, 1, 4, 38, 728, 26704
 * ```
 */
object ConnectedGraphs :
    CachedRecurrence<BigInteger> by ConnectedGraphsRecurrence

private object ConnectedGraphsRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ZERO
        1 -> BigInteger.ONE
        else -> {
            val total = BigInteger.TWO.pow(n * (n - 1) / 2)
            val subtract = (1 until n).fold(BigInteger.ZERO) { acc, k ->
                val term = Binomial(n - 1, k - 1) *
                        BigInteger.TWO.pow((n - k) * (n - k - 1) / 2) *
                        this(k)
                acc + term
            }
            total - subtract
        }
    }
}
