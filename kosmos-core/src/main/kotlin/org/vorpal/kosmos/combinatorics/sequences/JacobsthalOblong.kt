package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.math.bigIntSgn
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Jacobsthal–Oblong numbers** (or **Jacobsthal pronic numbers**) —
 * the products of consecutive [Jacobsthal] numbers:
 *
 * ```
 * Oₙ = Jₙ · Jₙ₊₁
 * ```
 *
 * Base cases:
 * ```
 * O₀ = 0,  O₁ = 0
 * ```
 *
 * First few terms:
 * ```
 * 0, 0, 1, 3, 15, 55, 231, 903, 3875, ...
 * ```
 *
 * ### Closed form
 * ```
 * Oₙ = (1/9) · (2ⁿ − (–1)ⁿ) · (2ⁿ⁺¹ − (–1)ⁿ⁺¹)
 * ```
 *
 * OEIS A001045 (Jacobsthal) and A007482 (Jacobsthal–Oblong)
 */
object JacobsthalOblong :
    CachedRecurrence<BigInteger> by JacobsthalOblongRecurrence,
    CachedClosedForm<BigInteger> by JacobsthalOblongClosedForm

private object JacobsthalOblongRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else -> Jacobsthal(n) * Jacobsthal(n + 1)
    }
}

private object JacobsthalOblongClosedForm : CachedClosedFormImplementation<BigInteger>() {
    private val NINE = BigInteger.valueOf(9L)
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        else -> {
            val a = BigInteger.ONE.shl(n)
            val b = bigIntSgn(n)
            val c = a.shl(1)
            val d = -b
            (a - b) * (c - d) / NINE
        }
    }
}
