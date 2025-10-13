package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Motzkin numbers** Mₙ.
 *
 * Count the number of lattice paths from (0,0) to (n,0)
 * using steps (1,1), (1,0), and (1,−1) that never go below the x-axis.
 *
 * Equivalently:
 * - The number of non-crossing chord diagrams on n points.
 * - The number of non-intersecting chords among n labeled points on a circle.
 *
 * ### Recurrence
 * ```
 * M₀ = 1, M₁ = 1
 * Mₙ = Mₙ₋₁ + Σᵢ₌₀ⁿ⁻² Mᵢ · Mₙ₋₂₋ᵢ
 * ```
 *
 * ### Closed form
 * ```
 * Mₙ = Σₖ₌₀⌊n/2⌋ (1 / (k+1)) · C(n, 2k) · C(2k, k)
 * ```
 *
 * ### First values
 * ```
 * 1, 1, 2, 4, 9, 21, 51, 127, 323, ...
 * ```
 *
 * OEIS A001006
 */
object Motzkin :
    CachedRecurrence<BigInteger> by MotzkinRecurrence,
    CachedClosedForm<BigInteger> by MotzkinClosedForm

private object MotzkinRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0, 1 -> BigInteger.ONE
        else -> (0 until n - 1).fold(this(n - 1)) { acc, i ->
            acc + this(i) * this(n - 2 - i)
        }
    }
}

private object MotzkinClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        (0..(n / 2)).fold(BigInteger.ZERO) { acc, k ->
            acc + Binomial(n, 2 * k) * Binomial(2 * k, k) / BigInteger.valueOf(k + 1L)
        }
}