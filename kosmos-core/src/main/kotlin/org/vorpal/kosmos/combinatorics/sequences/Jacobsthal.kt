package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.math.bigIntSgn
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedLinearRecurrenceImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import java.math.BigInteger

/**
 * **Jacobsthal numbers** Jₙ:
 *
 * Satisfy the linear recurrence:
 * ```
 * J₀ = 0,  J₁ = 1,
 * Jₙ = Jₙ₋₁ + 2·Jₙ₋₂
 * ```
 *
 * ### Closed form
 * ```
 * Jₙ = (2ⁿ − (−1)ⁿ) / 3
 * ```
 *
 * First few terms:
 * ```
 * 0, 1, 1, 3, 5, 11, 21, 43, 85, ...
 * ```
 *
 * OEIS A001045
 */
object Jacobsthal :
    CachedRecurrence<BigInteger> by JacobsthalRecurrence,
    CachedClosedForm<BigInteger> by JacobsthalClosedForm

private object JacobsthalRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ZERO, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(1, 2),
    constantTerm = BigInteger.ZERO,
    multiply = LeftAction { s, t -> s.toBigInteger() * t },
    add = BinOp(combine = BigInteger::add)
)

private object JacobsthalClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val THREE = BigInteger.valueOf(3L)
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else -> (BigInteger.ONE.shl(n) - bigIntSgn(n)) / THREE
    }
}