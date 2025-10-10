package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Jacobsthal–Lucas numbers** — the companion sequence to the [Jacobsthal] numbers,
 * defined by the same recurrence:
 *
 *     j₀ = 2, j₁ = 1,
 *     jₙ₊₁ = jₙ + 2·jₙ₋₁
 *
 * The first few terms are:
 *
 *     2, 1, 5, 7, 17, 31, 65, 127, 257, ...
 *
 * Like the Jacobsthal numbers, they admit the closed form:
 *
 *     jₙ = 2ⁿ + (–1)ⁿ
 *
 * These numbers are a **Lucas-type sequence** with parameters (P, Q) = (1, –2),
 * analogous to how the Lucas numbers relate to the Fibonacci sequence.
 *
 * They satisfy identities such as:
 *
 *     jₙ = Jₙ₊₁ + 2·Jₙ
 *     jₙ² – 8·Jₙ² = 9(–1)ⁿ
 *
 * @see Jacobsthal for the base sequence.
 * @see JacobsthalOblong for the oblong (product) sequence.
 */
object JacobsthalLucas : Recurrence<BigInteger> by LinearRecurrence.forBigInt(
    initial = listOf(2, 1),
    coeffs = listOf(1, 2)
) {
    fun closedForm(n: Int): BigInteger =
        BigInteger.ONE.shl(n) + bigIntSgn(n)
}
