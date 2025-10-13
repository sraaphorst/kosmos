package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.valueOf

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
    private val primes = sequenceOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)

    override fun recursiveCalculator(n: Int): BigInteger = when {
        n == 0 -> ONE
        else -> this(n - 1) * valueOf(primes.elementAt(n - 1).toLong())
    }
}