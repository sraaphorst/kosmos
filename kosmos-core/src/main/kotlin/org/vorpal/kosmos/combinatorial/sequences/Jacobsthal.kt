package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Jacobsthal numbers** Jₙ:
 * satisfy the recurrence:
 * ```
 * J₀ = 0,  J₁ = 1,
 * Jₙ = Jₙ₋₁ + 2·Jₙ₋₂
 * ```
 *
 * Closed form:
 * ```
 * Jₙ = (2ⁿ − (−1)ⁿ) / 3
 * ```
 *
 * OEIS A001045
 */
object Jacobsthal :
    CachedLinearSequence(
        initial = listOf(BigInteger.ZERO, BigInteger.ONE),
        coefficients = listOf(BigInteger.ONE, BigInteger.TWO)
    ),
    CachedClosedForm {
    private val THREE = BigInteger.valueOf(3L)

    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else  -> (BigInteger.ONE.shl(n) - bigIntSgn(n)) / THREE
    }
}
