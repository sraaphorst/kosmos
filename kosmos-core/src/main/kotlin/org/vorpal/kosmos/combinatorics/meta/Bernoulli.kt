package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.std.Rational

/**
 * **Bernoulli numbers** Bₙ⁽±⁾:
 * appear in the expansion of x / (eˣ − 1) and related generating functions.
 *
 * Two conventions exist:
 * - **B⁻**: “classical” form with B₁ = −1/2
 * - **B⁺**: “alternate” form with B₁ = +1/2
 *
 * Recurrence:
 * ```
 * B₀ = 1
 * Σₖ₌₀ⁿ binom(n + 1, k) · Bₖ = 0  (for n > 0)
 * ```
 *
 * Closed form (via ζ):
 * ```
 * B₂ₙ = (−1)ⁿ⁺¹ · 2 · (2n)! / (2π)²ⁿ · ζ(2n)
 * B₂ₙ₊₁ = 0 for n ≥ 1
 * ```
 *
 * OEIS:
 * - A027641 — classical Bernoulli numbers (B⁻)
 * - A164555 — alternate Bernoulli numbers (B⁺)
 */
sealed class BernoulliBase(private val b1: Rational) :
    CachedRecurrence<Rational> by object : CachedRecurrenceImplementation<Rational>() {
        override fun recursiveCalculator(n: Int): Rational = when (n) {
            0 -> Rational.ONE
            1 -> b1
            else -> {
                // Σ_{k=0}^{n−1} binom(n+1, k) * B_k
                val sum = (0 until n).fold(Rational.ZERO) { acc, k ->
                    acc + Rational.of(Binomial(n + 1, k)) * this(k)
                }
                -sum / Rational.of(n + 1)
            }
        }
    }

object BernoulliMinus : BernoulliBase(Rational.of(-1, 2))
object BernoulliPlus : BernoulliBase(Rational.of(1, 2))
