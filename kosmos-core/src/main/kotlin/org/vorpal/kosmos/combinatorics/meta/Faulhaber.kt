package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.core.rational.Rational

/**
 * **Faulhaber’s formula** — closed-form power sums.
 *
 * Computes:
 * ```
 * Sₚ(n) = 1ᵖ + 2ᵖ + 3ᵖ + ⋯ + nᵖ
 * ```
 *
 * using Bernoulli polynomials:
 * ```
 * Sₚ(n) = (1 / (p + 1)) · [ Bₚ₊₁(n + 1) − Bₚ₊₁(0) ]
 * ```
 *
 * where Bₙ(x) are the **Bernoulli polynomials** of the first kind.
 *
 * Example:
 * ```
 * S₁(n) = n(n + 1) / 2
 * S₂(n) = n(n + 1)(2n + 1) / 6
 * S₃(n) = (n²(n + 1)²) / 4
 * ```
 *
 * OEIS:
 * - A000217, A000290, A001106 (triangular, square, cubic pyramidal numbers)
 */
object Faulhaber {
    operator fun invoke(p: Int, n: Int): Rational = when {
        p < 0 || n < 0 -> Rational.ZERO
        else -> {
            val bNext = BernoulliPolynomial(p + 1, Rational.of(n + 1))
            val bZero = BernoulliPolynomial.atZero(p + 1)
            (bNext - bZero) / Rational.of(p + 1)
        }
    }
}
