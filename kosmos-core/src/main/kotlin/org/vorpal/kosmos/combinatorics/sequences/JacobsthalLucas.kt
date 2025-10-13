package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedLinearRecurrenceImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Jacobsthal–Lucas numbers** — the companion sequence to the [Jacobsthal] numbers,
 * defined by the same recurrence:
 *
 * ```
 * j₀ = 2, j₁ = 1,
 * jₙ = jₙ₋₁ + 2·jₙ₋₂
 * ```
 *
 * The first few terms are:
 * ```
 * 2, 1, 5, 7, 17, 31, 65, 127, 257, ...
 * ```
 *
 * Like the Jacobsthal numbers, they admit the closed form:
 * ```
 * jₙ = 2ⁿ + (–1)ⁿ
 * ```
 *
 * These numbers are a **Lucas-type sequence** with parameters (P, Q) = (1, –2),
 * analogous to how the Lucas numbers relate to the Fibonacci sequence.
 *
 * They satisfy identities such as:
 * ```
 * jₙ = Jₙ₊₁ + 2·Jₙ
 * jₙ² – 8·Jₙ² = 9(–1)ⁿ
 * ```
 *
 * OEIS A014551
 */
object JacobsthalLucas :
    CachedRecurrence<BigInteger> by JacobsthalLucasRecurrence,
    CachedClosedForm<BigInteger> by JacobsthalLucasClosedForm


private object JacobsthalLucasRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.TWO, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(1, 2),
    zero = BigInteger.ZERO,
    multiply = Action({ s, t -> s.toBigInteger() * t }),
    add = BinOp(BigInteger::add)
)

private object JacobsthalLucasClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        n == 0 -> BigInteger.TWO
        else -> BigInteger.ONE.shl(n) + bigIntSgn(n)
    }
}
