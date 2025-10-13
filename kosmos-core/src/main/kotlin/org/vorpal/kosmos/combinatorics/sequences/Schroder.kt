package org.vorpal.kosmos.combinatorics.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorics.arrays.SchroderTriangle
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation

/**
 * **Large Schröder numbers** Sₙ.
 *
 * Count the number of lattice paths from (0,0) to (n,n)
 * using steps (1,0), (0,1), and (1,1) that never rise above y = x.
 *
 * Relationship:
 * ```
 * Sₙ = Σₖ₌₀ⁿ SchroderTriangle(n, k)
 * ```
 *
 * Recurrence (equivalently):
 * ```
 * S₀ = 1
 * Sₙ = Sₙ₋₁ + Σₖ₌₀ⁿ⁻¹ Sₖ · Sₙ₋₁₋ₖ
 * ```
 *
 * First values:
 * 1, 2, 6, 22, 90, 394, 1806, 8558, 41586, 206098, ...
 *
 * OEIS A006318
 */
object Schroder :
    CachedRecurrence<BigInteger> by SchroderRecurrence

private object SchroderRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        n == 0 -> BigInteger.ONE
        else   -> SchroderTriangle.row(n).sumOf { it }
    }
}
