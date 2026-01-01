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
 * Infinite sequence of **Lucas numbers** Lₙ.
 *
 * Defined by the same recurrence as the Fibonacci sequence:
 *
 *     L₀ = 2, L₁ = 1,
 *     Lₙ = Lₙ₋₁ + Lₙ₋₂  for n ≥ 2
 *
 * Closed form (Lucas–Binet formula):
 *
 *     Lₙ = φⁿ + (1 − φ)ⁿ,  where φ = (1 + √5) / 2
 *
 * Properties:
 * - Shares the Fibonacci recurrence but with different seeds.
 * - All Lucas numbers are integers despite involving √5 in the closed form.
 * - Lₙ = Fₙ₋₁ + Fₙ₊₁, where Fₙ are Fibonacci numbers.
 * - They alternate in parity: even–odd–odd–even–odd–odd–even–…
 *
 * First few terms:
 *   2, 1, 3, 4, 7, 11, 18, 29, 47, 76, 123, ...
 *
 * Growth:
 *   Lₙ ≈ φⁿ  (same as Fibonacci)
 *
 * Related:
 * - Fibonacci sequence (same recurrence, different seeds)
 * - Pell sequence (different coefficient)
 * - Tribonacci sequence (order-3 analog)
 */
object Lucas :
    CachedRecurrence<BigInteger> by LucasRecurrence,
    CachedClosedForm<BigInteger> by LucasClosedForm

private object LucasRecurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.TWO, BigInteger.ONE),
    selectors = listOf(-1, -2),
    coefficients = listOf(1, 1),
    constantTerm = BigInteger.ZERO,
    multiply = LeftAction { s, t -> s.toBigInteger() * t },
    add = BinOp(BigInteger::add)
)

private object LucasClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt5 = BigDecimal(5).sqrt(mc)
    private val phi = (BigDecimal.ONE + sqrt5).divide(BigDecimal(2), mc)
    private val psi = (BigDecimal.ONE - sqrt5).divide(BigDecimal(2), mc)

    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> error("Cannot calculate Lucas($n).")
        else -> (phi.pow(n, mc) + psi.pow(n, mc))
            .setScale(0, RoundingMode.HALF_UP)
            .toBigInteger()
    }
}
