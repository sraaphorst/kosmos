package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedLinearRecurrenceImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * **Pell–Lucas numbers** Qₙ:
 *
 * Q₀ = 1, Q₁ = 1, Qₙ = 2·Qₙ₋₁ + Qₙ₋₂  (n ≥ 2)
 *
 * Closed form:
 *   Qₙ = ½·((1+√2)ⁿ + (1−√2)ⁿ)
 */
object PellLucas :
    CachedRecurrence<BigInteger> by PellLucasRecurrence,
    CachedClosedForm<BigInteger> by PellLucasClosedForm

private object PellLucasRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ONE, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(2, 1),
    zero = BigInteger.ZERO,
    multiply = Action({ s, t -> s.toBigInteger() * t }),
    add = BinOp(BigInteger::add)
)

private object PellLucasClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt2 = BigDecimal(2).sqrt(mc)
    private val a = BigDecimal.ONE + sqrt2
    private val b = BigDecimal.ONE - sqrt2
    private val two = BigDecimal(2)

    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else  -> (a.pow(n, mc) + b.pow(n, mc)).divide(two, mc)
            .setScale(0, RoundingMode.HALF_UP)
            .toBigInteger()
    }
}