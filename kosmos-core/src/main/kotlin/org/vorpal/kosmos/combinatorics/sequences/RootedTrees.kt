package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Rooted trees** — OEIS A000081.
 *
 * Counts the number of *unlabeled* rooted trees with n nodes.
 *
 * Recurrence:
 * ```
 * T(1) = 1
 * T(n) = (1 / (n − 1)) * Σₖ₌₁ⁿ⁻¹ (Σ_{d|k} d · T(d)) · T(n − k)
 * ```
 *
 * This is an application of Pólya’s theorem and tree species enumeration.
 *
 * First few values:
 * ```
 * n : 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
 * T : 1, 1, 2, 4, 9, 20, 48, 115, 286, 719
 * ```
 */
object RootedTrees :
    CachedRecurrence<BigInteger> by RootedTreesRecurrence

private object RootedTreesRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ZERO
        1 -> BigInteger.ONE
        else -> {
            var sum = BigInteger.ZERO
            for (k in 1 until n) {
                val inner = (1..k)
                    .filter { k % it == 0 }
                    .fold(BigInteger.ZERO) { acc, d -> acc + BigInteger.valueOf(d.toLong()) * this(d) }
                sum += inner * this(n - k)
            }
            sum / BigInteger.valueOf((n - 1).toLong())
        }
    }
}
