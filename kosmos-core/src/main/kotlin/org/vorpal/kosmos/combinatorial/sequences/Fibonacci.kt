package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * **Fibonacci numbers** Fₙ:
 * F₀ = 0, F₁ = 1, Fₙ = Fₙ₋₁ + Fₙ₋₂
 *
 * Closed form (Binet’s formula):
 * ```
 * Fₙ = (φⁿ − ψⁿ) / √5
 * φ = (1 + √5)/2, ψ = (1 − √5)/2
 * ```
 *
 * OEIS A000045
 */
object Fibonacci :
    CachedLinearSequence(
        initial = listOf(BigInteger.ZERO, BigInteger.ONE),
        coefficients = listOf(BigInteger.ONE, BigInteger.ONE)
    ),
    CachedClosedForm {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt5 = BigDecimal(5).sqrt(mc)
    private val phi = (BigDecimal.ONE + sqrt5).divide(BigDecimal(2), mc)
    private val psi = (BigDecimal.ONE - sqrt5).divide(BigDecimal(2), mc)

    /** Binet’s closed form for Fₙ. */
    override fun closedFormCalculator(n: Int): BigInteger {
        if (n < 0) return BigInteger.ZERO
        val fn = (phi.pow(n, mc) - psi.pow(n, mc)).divide(sqrt5, mc)
        return fn.setScale(0, RoundingMode.HALF_UP).toBigInteger()
    }
}