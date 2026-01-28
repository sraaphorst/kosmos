package org.vorpal.kosmos.numbertheory.approximation

import org.vorpal.kosmos.combinatorics.sequences.Pell
import org.vorpal.kosmos.combinatorics.sequences.PellLucas
import org.vorpal.kosmos.core.rational.Rational

/**
 * Approximations to √2 derived from the convergents of its continued fraction expansion:
 *
 * `[1; 2, 2, 2, 2, ...]`.
 *
 * Each convergent is given by the ratio of the [Pell–Lucas][org.vorpal.kosmos.combinatorics.sequences.PellLucas] and [Pell][org.vorpal.kosmos.combinatorics.sequences.Pell] numbers:
 *
 *     √2 ≈ Qₙ / Pₙ
 *
 * with:
 *
 *     Q₀ = 1, Q₁ = 1, Qₙ₊₁ = 2·Qₙ + Qₙ₋₁
 *     P₀ = 0, P₁ = 1, Pₙ₊₁ = 2·Pₙ + Pₙ₋₁
 *
 * This sequence yields rational numbers converging alternately from above and below √2:
 *
 *     1/1, 3/2, 7/5, 17/12, 41/29, 99/70, 239/169, ...
 *
 * The difference |√2 − Qₙ/Pₙ| decreases exponentially.
 */
object Sqrt2Approximations :
    Sequence<Rational> by (
            PellLucas.zip(Pell)
                .drop(1)
                .map { (n, d) -> Rational.of(n, d) }
    )