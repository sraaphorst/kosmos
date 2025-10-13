package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Factorial
import org.vorpal.kosmos.combinatorics.arrays.StirlingFirst
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.std.Rational
import org.vorpal.kosmos.std.bigIntSgn

/**
 * **Harmonic numbers** Hₙ:
 * the sum of the reciprocals of the first n positive integers:
 *
 * ```
 * Hₙ = 1 + 1/2 + 1/3 + ... + 1/n
 * ```
 *
 * Recurrence:
 * ```
 * H₀ = 0
 * Hₙ = Hₙ₋₁ + 1/n
 * ```
 *
 * Closed form (in terms of signed Stirling numbers of the first kind):
 * ```
 * Hₙ = (−1)ⁿ · s(n+1, 2) / n!
 * ```
 *
 * Approximation for large n:
 * ```
 * Hₙ ≈ ln(n) + γ + 1/(2n) − 1/(12n²)
 * ```
 *
 * First values:
 * ```
 * 0, 1, 3/2, 11/6, 25/12, 137/60, ...
 * ```
 *
 * OEIS A001008 (numerators), A002805 (denominators), A001008/A002805 = A001008/A002805 (harmonic numbers)
 */
object Harmonic :
    CachedRecurrence<Rational> by HarmonicRecurrence,
    CachedClosedForm<Rational> by HarmonicClosedForm

private object HarmonicRecurrence : CachedRecurrenceImplementation<Rational>() {
    override fun recursiveCalculator(n: Int): Rational = when (n) {
        0 -> Rational.ZERO
        else -> this(n - 1) + Rational.of(1, n)
    }
}

private object HarmonicClosedForm : CachedClosedFormImplementation<Rational>() {
    override fun closedFormCalculator(n: Int): Rational = when (n) {
        0 -> Rational.ZERO
        else -> {
            val s1 = StirlingFirst(n + 1, 2)
            Rational.of(bigIntSgn(n) * s1, Factorial(n))
        }
    }
}
