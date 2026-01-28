package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * **Generalized harmonic numbers** Hₙ^(m):
 * the sum of reciprocals of the first n integers raised to power m:
 *
 * ```
 * Hₙ^(m) = 1 + 1/2ᵐ + 1/3ᵐ + … + 1/nᵐ
 * ```
 *
 * Recurrence:
 * ```
 * H₀^(m) = 0
 * Hₙ^(m) = Hₙ₋₁^(m) + 1/nᵐ
 * ```
 *
 * When m = 1, this reduces to the ordinary [Harmonic] numbers.
 * Note that this is not really an array, but a parameterized family of univariate sequences.
 */
object GeneralizedHarmonic {
    /** Returns the sequence Hₙ^(m) as a cached recurrence. */
    fun of(m: Int): CachedRecurrence<Rational> =
        object : CachedRecurrenceImplementation<Rational>() {
            override fun recursiveCalculator(n: Int): Rational = when (n) {
                0 -> Rational.ZERO
                else -> this(n - 1) + Rational.of(BigInteger.ONE, n.toBigInteger().pow(m))
            }
        }

    /** Direct non-cached evaluation (for one-off values). */
    operator fun invoke(n: Int, m: Int): Rational =
        (1..n).fold(Rational.ZERO) { acc, k ->
            acc + Rational.of(BigInteger.ONE, k.toBigInteger().pow(m))
        }
}