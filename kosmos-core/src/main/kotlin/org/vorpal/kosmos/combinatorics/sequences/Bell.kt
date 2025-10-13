package org.vorpal.kosmos.combinatorics.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorics.arrays.Aitken
import org.vorpal.kosmos.combinatorics.arrays.StirlingSecond
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation

/**
 * **Bell numbers** Bₙ:
 * count of all distinct partitions of an n-element set.
 *
 * Definitions:
 * ```
 * B₀ = 1
 * Bₙ = Σₖ₌₀ⁿ Aitken(n, k)
 * ```
 * or equivalently (closed form):
 * ```
 * Bₙ = Σₖ₌₀ⁿ StirlingSecond(n, k)
 * ```
 *
 * Recurrence (Dobinski / Bell identity form):
 * ```
 * B₀ = 1
 * Bₙ₊₁ = Σₖ₌₀ⁿ (n choose k) * Bₖ
 * ```
 *
 * OEIS A000110
 */
object Bell :
    CachedRecurrence<BigInteger> by BellRecurrence,
    CachedClosedForm<BigInteger> by BellClosedForm

private object BellRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0  -> error("Cannot calculate Bell($n).")
        n == 0 -> BigInteger.ONE
        else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Aitken(n, k) }
    }
}

private object BellClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> error("Cannot calculate Bell($n).")
        n == 0 -> BigInteger.ONE
        else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + StirlingSecond(n, k) }
    }
}
