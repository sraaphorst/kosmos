package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
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
    CachedLinearSequence(
        initial = listOf(BigInteger.TWO, BigInteger.ONE),
        coefficients = listOf(BigInteger.ONE, BigInteger.ONE)
    ),
    CachedClosedForm {

    private val mc = MathContext(50, RoundingMode.HALF_EVEN)
    private val sqrt5 = BigDecimal(5).sqrt(mc)
    private val phi = (BigDecimal.ONE + sqrt5).divide(BigDecimal(2), mc)
    private val psi = (BigDecimal.ONE - sqrt5).divide(BigDecimal(2), mc)

    override fun closedFormCalculator(n: Int): BigInteger {
        if (n < 0) return BigInteger.ZERO
        val ln = phi.pow(n, mc) + psi.pow(n, mc)
        return ln.setScale(0, RoundingMode.HALF_UP).toBigInteger()
    }
}