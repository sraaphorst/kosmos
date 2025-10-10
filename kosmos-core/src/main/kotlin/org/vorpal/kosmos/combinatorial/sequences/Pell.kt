package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * **Pell numbers** — the integer sequence defined by the recurrence:
 *
 *     P₀ = 0, P₁ = 1,
 *     Pₙ₊₁ = 2·Pₙ + Pₙ₋₁
 *
 * The first few terms are:
 *
 *     0, 1, 2, 5, 12, 29, 70, 169, 408, ...
 *
 * The Pell numbers arise in several contexts:
 *
 * - As the **denominators** of the best rational approximations to √2:
 *
 *       √2 ≈ Qₙ / Pₙ
 *
 *   where `Qₙ` are the [Pell–Lucas][PellLucas] numbers.
 *
 * - As integer solutions (Pₙ, Pₙ₋₁) to the **Pell equation**
 *
 *       x² - 2y² = (–1)ⁿ
 *
 * - In continued fractions of √2:
 *
 *       √2 = [1; 2, 2, 2, 2, ...]
 *
 * The ratio of consecutive Pell numbers converges to √2.
 *
 * @see PellLucas for the companion sequence generating the numerators of √2 approximants.
 */
object Pell : Recurrence<BigInteger> by LinearRecurrence.forBigIntFromLong(
    initial = listOf(0, 1),
    coeffs = listOf(2, 1)
)
