package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
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
    CachedLinearSequence(
        initial = listOf(BigInteger.ONE, BigInteger.ONE),
        coefficients = listOf(BigInteger.TWO, BigInteger.ONE)
    ),
    CachedClosedForm {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt2 = BigDecimal(2).sqrt(mc)
    private val a = BigDecimal.ONE + sqrt2
    private val b = BigDecimal.ONE - sqrt2
    private val two = BigDecimal(2)

    override fun closedFormCalculator(n: Int): BigInteger {
        if (n < 0) return BigInteger.ZERO
        val qn = (a.pow(n, mc) + b.pow(n, mc)).divide(two, mc)
        return qn.setScale(0, RoundingMode.HALF_UP).toBigInteger()
    }
}