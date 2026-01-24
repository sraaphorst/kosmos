package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.core.Rational

/**
 * **Genocchi numbers** Gₙ — OEIS A001469.
 *
 * Defined by the exponential generating function:
 * ```
 *    2x / (eˣ + 1) = Σₙ₌₁^∞ Gₙ xⁿ / n!
 * ```
 *
 * Related to the **Bernoulli numbers** Bₙ⁽⁻⁾ by:
 * ```
 *    Gₙ = 2(1 − 2ⁿ) Bₙ⁽⁻⁾
 * ```
 * where B₁⁽⁻⁾ = −1/2 (the “minus” convention).
 *
 * The first few values are:
 * ```
 *    0, 1, −1, 0, 3, 0, −17, 0, 155, 0, −2073, ...
 * ```
 *
 * Notes:
 * - Gₙ is zero for all odd n > 1.
 * - Gₙ shares sign alternation with the Bernoulli numbers.
 */
object Genocchi :
    CachedClosedForm<Rational> by GenocchiClosedForm

private object GenocchiClosedForm : CachedClosedFormImplementation<Rational>() {
    override fun closedFormCalculator(n: Int): Rational = when {
        n <= 0 -> Rational.ZERO
        else -> Rational.of(2) *
                (Rational.ONE - Rational.of(2).pow(n)) *
                BernoulliMinus(n)
    }
}
