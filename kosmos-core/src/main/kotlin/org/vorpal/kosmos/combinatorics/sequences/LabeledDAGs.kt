package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.core.math.bigIntSgn
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Labeled DAGs** — OEIS A003024.
 *
 * Number of *labeled directed acyclic graphs* (DAGs) with n vertices.
 *
 * Recurrence:
 * ```
 * D(0) = 1
 * D(n) = Σₖ₌₁ⁿ (−1)^{k+1} * binom(n, k) * 2^{k(n−k)} * D(n − k)
 * ```
 *
 * First few terms:
 * ```
 * n : 0, 1, 2, 3, 4, 5, 6
 * D : 1, 1, 3, 25, 543, 29281, 3781503
 * ```
 *
 * Used in partial orders, topological sorts, and causal inference models.
 */
object LabeledDAGs :
    CachedRecurrence<BigInteger> by LabeledDAGsRecurrence

private object LabeledDAGsRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        else ->
            (1..n).fold(BigInteger.ZERO) { acc, k ->
                val term = Binomial(n, k) * BigInteger.ONE.shl(k * (n - k)) * this(n - k)
                acc + bigIntSgn(k - 1) * term
        }
    }
}
