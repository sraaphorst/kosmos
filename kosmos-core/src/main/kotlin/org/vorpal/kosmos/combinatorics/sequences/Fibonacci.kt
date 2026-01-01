package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.ops.LeftAction
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
    CachedRecurrence<BigInteger> by FibonacciRecurrence,
    CachedClosedForm<BigInteger> by FibonacciClosedForm

private object FibonacciRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ZERO, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(1, 1),
    constantTerm = BigInteger.ZERO,
    multiply = LeftAction { s, t -> s.toBigInteger() * t },
    add = BinOp(BigInteger::add)
)

private object FibonacciClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt5 = BigDecimal(5).sqrt(mc)
    private val phi = (BigDecimal.ONE + sqrt5).divide(BigDecimal.TWO, mc)
    private val psi = (BigDecimal.ONE - sqrt5).divide(BigDecimal.TWO, mc)

    /** Binet’s closed form for Fₙ. */
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> error("Cannot calculate Fibonacci($n).")
        n == 0 -> BigInteger.ZERO
        else -> (phi.pow(n, mc) - psi.pow(n, mc)).divide(sqrt5, mc)
            .setScale(0, RoundingMode.HALF_UP)
            .toBigInteger()
    }
}
