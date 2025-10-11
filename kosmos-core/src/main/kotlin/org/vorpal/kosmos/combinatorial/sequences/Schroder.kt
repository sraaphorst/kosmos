package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.arrays.SchroderTriangle
import org.vorpal.kosmos.combinatorial.recurrence.CachedRecursiveSequence

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
object Schroder : CachedRecursiveSequence() {
    override val initial = listOf(BigInteger.ONE)

    /** Memoized row-sum definition via the Schröder triangle. */
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n == 0 -> BigInteger.ONE
        else   -> SchroderTriangle.row(n).sumOf { it }
    }

    // Alternatively, we could do:
//    override fun recursiveCalculator(n: Int): BigInteger = when {
//        n == 0 -> BigInteger.ONE
//        else -> Schroder(n - 1) + (0 until n).fold(BigInteger.ZERO) { acc, k ->
//            acc + Schroder(k) * Schroder(n - 1 - k)
//        }
//    }
}
