package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.arrays.SchroderTriangle
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.memoization.memoize

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
object Schroder : Recurrence<BigInteger> {

    /** Memoized row-sum definition via the Schröder triangle. */
    private val recursiveCache = memoize<Int, BigInteger> { n ->
        when (n) {
            0 -> BigInteger.ONE
            else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + SchroderTriangle(n, k) }
        }
    }

    operator fun invoke(n: Int): BigInteger = recursiveCache(n)

    override fun iterator(): Iterator<BigInteger> = object : Iterator<BigInteger> {
        private var n = 0
        override fun hasNext(): Boolean = true
        override fun next(): BigInteger = recursiveCache(n++)
    }
}
