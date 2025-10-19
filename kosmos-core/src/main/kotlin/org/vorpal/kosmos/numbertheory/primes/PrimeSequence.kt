package org.vorpal.kosmos.numbertheory.primes

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * p₀ = 2, p₁ = 3, p₂ = 5, ...
 *
 * NOTE: This sequence is **0-based** to match Recurrence<T>.
 * Use RecurrenceLattice to expose a **1-based** lattice view when needed.
 */
object PrimeSequence : CachedRecurrenceImplementation<BigInteger>(), CachedRecurrence<BigInteger> {

    /**
     * Recursively (via prior primes) compute p_n and rely on the base cache.
     * p_0 = 2; for n>0, p_n is the next prime after p_{n-1}.
     */
    override fun recursiveCalculator(n: Int): BigInteger {
        require(n >= 0) { "Prime index must be ≥ 0 (got $n)" }
        return when (n) {
            0 -> BigInteger.TWO
            else -> nextPrime(after = this(n - 1))
        }
    }

    override fun clearRecurrenceCache() = super.clearRecurrenceCache()

    // --- Helpers ---

    private fun nextPrime(after: BigInteger): BigInteger {
        var c = after + BigInteger.ONE
        while (!isPrimeByKnownPrimes(c)) c += BigInteger.ONE
        return c
    }

    /**
     * Trial division by already-known primes from the cache:
     * divide by p_0, p_1, ..., up to sqrt(candidate).
     */
    private fun isPrimeByKnownPrimes(candidate: BigInteger): Boolean {
        if (candidate <= BigInteger.ONE) return false
        if (candidate == BigInteger.TWO) return true
        if (candidate.and(BigInteger.ONE) == BigInteger.ZERO) return false

        var i = 0
        while (true) {
            val p = this(i)                                // uses cache
            if (p * p > candidate) break
            if (candidate % p == BigInteger.ZERO) return false
            i++
        }
        return true
    }
}
