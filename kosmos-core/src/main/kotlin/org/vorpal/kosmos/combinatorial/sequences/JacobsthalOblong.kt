package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.CachedNonlinearRecurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Jacobsthal–Oblong numbers** (or **Jacobsthal pronic numbers**) —
 * the products of consecutive [Jacobsthal] numbers:
 *
 *     Oₙ = Jₙ · Jₙ₊₁
 *
 * with base cases:
 *
 *     O₀ = 0, O₁ = 0,
 *     Oₙ₊₁ = Jₙ₊₁ (Jₙ₊₁ + Jₙ)
 *
 * Equivalently, they satisfy the nonlinear recurrence:
 *
 *     Oₙ₊₁ = (√(Oₙ + Jₙ²))² + 2Oₙ  // using Jₙ = (2ⁿ – (–1)ⁿ) / 3
 *
 * The first few terms are:
 *
 *     0, 0, 1, 3, 15, 55, 231, 903, 3875, ...
 *
 * The closed form is:
 *
 *     Oₙ = (1/9) · (2ⁿ – (–1)ⁿ) · (2ⁿ⁺¹ – (–1)ⁿ⁺¹)
 */
object JacobsthalOblong :
    CachedNonlinearRecurrence(
        initial = listOf(BigInteger.ZERO, BigInteger.ONE),
        next = { terms ->
            // We have O₀..Oₙ; compute Oₙ₊₁ using Jacobsthal closed form for Jₙ, Jₙ₊₁
            val n = terms.lastIndex
            Jacobsthal(n) * Jacobsthal(n + 1)
        }
), CachedClosedForm {
    /**
     * Closed form for Jacobsthal–Oblong numbers:
     *     Oₙ = (1/9) · (2ⁿ – (–1)ⁿ) · (2ⁿ⁺¹ – (–1)ⁿ⁺¹)
     */
    override fun closedFormCalculator(n: Int): BigInteger {
        if (n < 0) return BigInteger.ZERO
        val a = BigInteger.ONE.shl(n)
        val b = bigIntSgn(n)
        val c = a.shl(1)
        val d = -b
        return (a - b) * (c - d) / BigInteger.valueOf(9)
    }
}
