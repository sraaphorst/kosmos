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
 * **Pell numbers** Pₙ:
 *
 * P₀ = 0, P₁ = 1,  Pₙ = 2·Pₙ₋₁ + Pₙ₋₂  (n ≥ 2)
 *
 * Closed form:
 *   Pₙ = ((1+√2)ⁿ − (1−√2)ⁿ) / (2√2)
 */
object Pell :
    CachedRecurrence<BigInteger> by PellRecurrence,
    CachedClosedForm<BigInteger> by PellClosedForm

private object PellRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ZERO, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(2, 1),
    zero = BigInteger.ZERO,
    multiply = Action({ s, t -> s.toBigInteger() * t }),
    add = BinOp(BigInteger::add)
)

private object PellClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt2 = BigDecimal(2).sqrt(mc)
    private val a = (BigDecimal.ONE + sqrt2)
    private val b = (BigDecimal.ONE - sqrt2)
    private val twoSqrt2 = BigDecimal(2).multiply(sqrt2, mc)

    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else -> a.pow(n, mc).subtract(b.pow(n, mc), mc)
            .divide(twoSqrt2, mc)
            .setScale(0, RoundingMode.HALF_UP)
            .toBigInteger()
    }
}
