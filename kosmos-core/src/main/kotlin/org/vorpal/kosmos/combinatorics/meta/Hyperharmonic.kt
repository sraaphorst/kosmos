package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.array.CachedBivariateArray
import org.vorpal.kosmos.std.Rational

/**
 * **Hyperharmonic numbers** Hₙ^(r):
 * generalization of harmonic numbers by order r.
 *
 * Recurrence:
 * ```
 * H₀^(r) = 0
 * Hₙ^(1) = Harmonic(n)
 * Hₙ^(r) = Hₙ₋₁^(r) + Hₙ^(r−1)
 * ```
 *
 * Closed form:
 * ```
 * Hₙ^(r) = binom(n + r − 1, r − 1) · (Hₙ₊ᵣ₋₁ − Hᵣ₋₁)
 * ```
 *
 * OEIS A001008 (r = 2), general family A175037
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object Hyperharmonic : CachedBivariateArray<Rational>() {
    override fun recursiveCalculator(n: Int, r: Int): Rational = when {
        n <= 0 -> Rational.ZERO
        r == 1 -> Harmonic(n)
        else -> this(n - 1, r) + this(n, r - 1)
    }

    override fun closedFormCalculator(n: Int, r: Int): Rational =
        if (n <= 0) Rational.ZERO
        else {
            val bin = Binomial(n + r - 1, r - 1)
            val diff = Harmonic(n + r - 1) - Harmonic(r - 1)
            Rational.of(bin) * diff
        }
}
