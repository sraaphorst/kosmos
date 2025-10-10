package org.vorpal.kosmos.combinatorial.sequences

import java.math.BigInteger
import org.vorpal.kosmos.combinatorial.arrays.Aitken
import org.vorpal.kosmos.combinatorial.arrays.StirlingSecond
import org.vorpal.kosmos.combinatorial.recurrence.ClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.memoization.memoize
import org.vorpal.kosmos.memoization.recursiveMemoize

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
object Bell : Recurrence<BigInteger>, ClosedForm<BigInteger> {
    /** Recursive definition using the Aitken (Bell) triangle. */
    private val recursiveCache = recursiveMemoize<Int, BigInteger> { self, n ->
        when (n) {
            0 -> BigInteger.ONE
            else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Aitken(n, k) }
        }
    }

    /** Closed form using Stirling numbers of the second kind. */
    private val closedFormCache = memoize<Int, BigInteger> { n ->
        when (n) {
            0 -> BigInteger.ONE
            else -> (0..n).fold(BigInteger.ZERO) { acc, k -> acc + StirlingSecond(n, k) }
        }
    }

    /** Returns Bₙ = Bell(n). */
    operator fun invoke(n: Int): BigInteger = recursiveCache(n)

    override fun iterator(): Iterator<BigInteger> = object : Iterator<BigInteger> {
        private var n = 0
        override fun hasNext(): Boolean = true
        override fun next(): BigInteger = recursiveCache(n++)
    }

    override fun closedForm(n: Int): BigInteger = closedFormCache(n)
}