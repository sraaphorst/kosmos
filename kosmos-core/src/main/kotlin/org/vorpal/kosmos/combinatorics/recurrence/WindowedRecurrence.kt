package org.vorpal.kosmos.combinatorics.recurrence

import java.math.BigInteger

/**
 * A recurrence that depends on a fixed number of previous terms.
 * Examples:
 *   - Fibonacci: aₙ = aₙ₋₁ + aₙ₋₂
 *   - Partition numbers: p(n) = p(n−1) + p(n−2) − p(n−5) − ...
 */
interface WindowedRecurrence : UnivariateRecurrence {
    /** The initial values for the recurrence. */
    val initial: List<BigInteger>
    val window: Int
        get() = initial.size
}
