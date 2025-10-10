package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Jacobsthal numbers** — the integer sequence defined by:
 *
 *     J₀ = 0, J₁ = 1,
 *     Jₙ₊₁ = Jₙ + 2·Jₙ₋₁
 *
 * The first few terms are:
 *
 *     0, 1, 1, 3, 5, 11, 21, 43, 85, 171, ...
 *
 * This sequence arises in binary recurrences and has the closed form:
 *
 *     Jₙ = (2ⁿ – (–1)ⁿ) / 3
 *
 * Properties:
 * - Every Jacobsthal number is the nearest integer to (2ⁿ / 3).
 * - Related to the binary representation of natural numbers — specifically, Jₙ
 *   counts the number of "00"-free binary strings of length n–1.
 * - The ratio Jₙ₊₁ / Jₙ converges to 2.
 *
 * Related sequences:
 * - [Jacobsthal–Lucas][JacobsthalLucas]: same recurrence, different initial conditions.
 * - [Jacobsthal–Oblong][JacobsthalOblong]: product of consecutive Jacobsthal numbers.
 */
object Jacobsthal : Recurrence<BigInteger> by LinearRecurrence.forBigInt(
    initial = listOf(0, 1),
    coeffs = listOf(1, 2)
) {
    private val THREE = BigInteger.valueOf(3L)

    /**
     * Jacobsthal numbers have a closed form expression.
     */
    fun closedForm(n: Int): BigInteger =
        (BigInteger.ONE.shl(n) - bigIntSgn(n)) / THREE
}
