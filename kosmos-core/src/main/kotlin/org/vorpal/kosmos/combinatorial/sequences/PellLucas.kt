package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * **Pell–Lucas numbers** — the companion sequence to the [Pell] numbers,
 * defined by the same recurrence:
 *
 *     Q₀ = 1, Q₁ = 1,
 *     Qₙ₊₁ = 2·Qₙ + Qₙ₋₁
 *
 * The first few terms are:
 *
 *     1, 1, 3, 7, 17, 41, 99, 239, 577, ...
 *
 * The Pell–Lucas numbers occur as the **numerators** of the best rational
 * approximations to √2:
 *
 *     √2 ≈ Qₙ / Pₙ
 *
 * where Pₙ are the [Pell] numbers.
 *
 * They satisfy the Diophantine identity:
 *
 *     Qₙ² - 2·Pₙ² = (–1)ⁿ
 *
 * and form a Lucas-type sequence with parameters (P, Q) = (2, –1).
 *
 * @see Pell for the companion denominator sequence.
 */
object PellLucas : Recurrence<BigInteger> by LinearRecurrence.forBigInt(
    initial = listOf(1, 1),
    coeffs = listOf(2, 1)
)
