package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
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
    CachedLinearSequence(
        initial = listOf(BigInteger.ZERO, BigInteger.ONE),
        coefficients = listOf(BigInteger.TWO, BigInteger.ONE)
    ),
    CachedClosedForm {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt2 = BigDecimal(2).sqrt(mc)
    private val a = (BigDecimal.ONE + sqrt2)
    private val b = (BigDecimal.ONE - sqrt2)
    private val twoSqrt2 = BigDecimal(2).multiply(sqrt2, mc)

    override fun closedFormCalculator(n: Int): BigInteger {
        if (n < 0) return BigInteger.ZERO
        val num = a.pow(n, mc).subtract(b.pow(n, mc), mc)
        val pn = num.divide(twoSqrt2, mc)
        return pn.setScale(0, RoundingMode.HALF_UP).toBigInteger()
    }
}