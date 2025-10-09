package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * Infinite sequence of **Lucas numbers** Lₙ.
 *
 * Defined by the same recurrence as the Fibonacci sequence:
 *
 *     L₀ = 2, L₁ = 1,
 *     Lₙ = Lₙ₋₁ + Lₙ₋₂  for n ≥ 2
 *
 * Closed form (Lucas–Binet formula):
 *
 *     Lₙ = φⁿ + (1 − φ)ⁿ,  where φ = (1 + √5) / 2
 *
 * Properties:
 * - Shares the Fibonacci recurrence but with different seeds.
 * - All Lucas numbers are integers despite involving √5 in the closed form.
 * - Lₙ = Fₙ₋₁ + Fₙ₊₁, where Fₙ are Fibonacci numbers.
 * - They alternate in parity: even–odd–odd–even–odd–odd–even–…
 *
 * First few terms:
 *   2, 1, 3, 4, 7, 11, 18, 29, 47, 76, 123, ...
 *
 * Growth:
 *   Lₙ ≈ φⁿ  (same as Fibonacci)
 *
 * Related:
 * - Fibonacci sequence (same recurrence, different seeds)
 * - Pell sequence (different coefficient)
 * - Tribonacci sequence (order-3 analog)
 */
object Lucas : Recurrence<BigInteger> by LinearRecurrence.forBigInt(
    initial = listOf(2, 1),
    coeffs = listOf(1, 1)
)

/** Lightweight 64-bit Fibonacci implementation (overflows around 92). */
object Lucas64 : Recurrence<Long> by LinearRecurrence.forLong(
    initial = listOf(2, 1),
    coeffs = listOf(1, 1)
)
