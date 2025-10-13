package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Euclid numbers** — product of the first n primes plus one.
 *
 * Defined recursively:
 * ```
 * E₀ = 2
 * Eₙ = (Π_{k=0}^{n−1} Eₖ) + 1
 * ```
 *
 * or equivalently using primes:
 * ```
 * Eₙ = p₁p₂…pₙ + 1
 * ```
 *
 * First few terms:
 * ```
 * 2, 3, 7, 43, 1807, 3263443, ...
 * ```
 *
 * Euclid used these in his proof that there are infinitely many primes.
 * OEIS A006862
 */
object Euclid :
    CachedRecurrence<BigInteger> by EuclidRecurrence

private object EuclidRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.TWO
        else -> (0 until n).fold(BigInteger.ONE) { acc, i -> acc * this(i) } + BigInteger.ONE
    }
}
