package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.arrays.Aitken
import org.vorpal.kosmos.combinatorial.arrays.StirlingSecond
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedFormSequence

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
object Bell : CachedClosedFormSequence() {
    override val initial = listOf(BigInteger.ONE)

    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Aitken(n, k) }
    }

    override fun closedFormCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + StirlingSecond(n, k) }
    }
}

