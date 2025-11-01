package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.numbertheory.primes.PrimeSequence
import java.math.BigInteger
import java.math.BigInteger.ONE

/**
 * **Primorial numbers** — product of the first n primes.
 * TODO: Replace the hardcoded primes with an actual prime generator from org.vorpal.kosmos.numbertheory.
 *
 * Recurrence:
 * ```
 * P₀ = 1
 * Pₙ = Pₙ₋₁ · primeₙ
 * ```
 *
 * First few terms:
 * ```
 * n: 0, 1, 2, 3, 4, 5, 6
 * P: 1, 2, 6, 30, 210, 2310, 30030
 * ```
 *
 * OEIS A002110
 */
object Primorial :
    CachedRecurrence<BigInteger> by PrimorialRecurrence

private object PrimorialRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n == 0 -> ONE
        else -> this(n - 1) * PrimeSequence(n - 1)
    }
}
