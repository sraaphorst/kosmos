package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Cake numbers** (also called *solid partition numbers*) — OEIS A000125.
 *
 * Number of regions into which n planes divide 3D space.
 *
 * Recurrence:
 * ```
 * C₀ = 1
 * Cₙ = Cₙ₋₁ + Tₙ₋₁, where Tₙ₋₁ = (n−1)(n)/2 + 1
 * ```
 *
 * Closed form:
 * ```
 * Cₙ = (n³ + 5n + 6) / 6
 * ```
 *
 * Examples:
 * ```
 * n: 0, 1, 2, 3, 4, 5, 6
 * C: 1, 2, 4, 8, 15, 26, 42
 * ```
 */
object Cake :
    CachedRecurrence<BigInteger> by CakeRecurrence,
    CachedClosedForm<BigInteger> by CakeClosedForm

private object CakeRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        else -> this(n - 1) + BigInteger.valueOf(n.toLong() * (n - 1) / 2 + 1)
    }
}

private object CakeClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        BigInteger.valueOf((n.toLong() * n * n + 5L * n + 6L) / 6L)
}
