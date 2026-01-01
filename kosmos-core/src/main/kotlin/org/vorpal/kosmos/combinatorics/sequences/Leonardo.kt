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
 * **Leonardo numbers** Lₙ:
 * L₀ = 1, L₁ = 1, Lₙ = Lₙ₋₁ + Lₙ₋₂ + 1
 *
 * Closed form:
 * ```
 * Lₙ₊₁ = 2 * (φⁿ⁺¹ − ψⁿ⁺¹) / √5 - 1
 * φ = (1 + √5)/2, ψ = (1 − √5)/2
 * ```
 *
 * The Leonardo numbers are an integral component of Dijkstra's smoothsort algorithm:
 * https://en.wikipedia.org/wiki/Smoothsort
 *
 * OEIS A001595
 */
object Leonardo :
    CachedRecurrence<BigInteger> by LeonardoRecurrence,
    CachedClosedForm<BigInteger> by LeonardoClosedForm

private object LeonardoRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ONE, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(1, 1),
    constantTerm = BigInteger.ONE,
    multiply = LeftAction { s, t -> s.toBigInteger() * t },
    add = BinOp(BigInteger::add)
)

private object LeonardoClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt5 = BigDecimal(5).sqrt(mc)
    private val phi = (BigDecimal.ONE + sqrt5).divide(BigDecimal.TWO, mc)
    private val psi = (BigDecimal.ONE - sqrt5).divide(BigDecimal.TWO, mc)

    /** Binet’s closed form for Fₙ. */
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> error("Cannot calculate Leonardo($n).")
        n == 0 -> BigInteger.ONE
        else -> ((BigDecimal.TWO * (phi.pow(n + 1, mc) - psi.pow(n + 1, mc))).divide(sqrt5, mc) - BigDecimal.ONE)
            .setScale(0, RoundingMode.HALF_UP)
            .toBigInteger()
    }
}
