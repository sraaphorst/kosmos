package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.ClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.memoization.memoize
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

/**
 * Infinite sequence of **Fibonacci numbers** Fₙ.
 *
 * Defined by:
 *   F(0) = 0, F(1) = 1,
 *   F(n) = F(n−1) + F(n−2)
 *
 * Recurrence coefficients: [1, 1]
 * Initial values: [0, 1]
 *
 * First few terms:
 *   0, 1, 1, 2, 3, 5, 8, 13, ...
 *
 * Properties:
 * - Linear recurrence with constant coefficients.
 * - Appears in nature and mathematics via the golden ratio φ.
 * - Exponential generating function: (eˣ - 1) / (eˣ + 1)
 */
object Fibonacci : Recurrence<BigInteger> by LinearRecurrence.forBigIntFromLong(
    initial = listOf(0, 1),
    coeffs = listOf(1, 1)
), ClosedForm<BigInteger> {
    private val phi = (BigDecimal.ONE + BigDecimal(5.0).sqrt(MathContext.DECIMAL64)) / BigDecimal.TWO
    private val psi = (BigDecimal.ONE - BigDecimal(5.0).sqrt(MathContext.DECIMAL64)) / BigDecimal.TWO
    private val closedFormCache = memoize<Int, BigInteger> { n  ->
        ((phi.pow(n) - psi.pow(n))/(phi - psi)).toBigInteger()
    }

    override fun closedForm(n: Int): BigInteger = closedFormCache(n)
}
