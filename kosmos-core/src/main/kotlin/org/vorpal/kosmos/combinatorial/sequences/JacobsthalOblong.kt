package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.NonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import org.vorpal.kosmos.memoization.memoize
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
 *
 * @see Jacobsthal
 * @see JacobsthalLucas
 */
object JacobsthalOblong : Recurrence<BigInteger> by NonlinearRecurrence(
    initial = listOf(BigInteger.ZERO, BigInteger.ZERO),
    next = { terms ->
        // We have O₀..Oₙ; compute Oₙ₊₁ using Jacobsthal closed form for Jₙ, Jₙ₊₁
        val n = terms.lastIndex
        val jn = JacobsthalClosedForm(n)
        val jn1 = JacobsthalClosedForm(n + 1)
        jn * jn1
    }
) {
    /**
     * Closed form for Jacobsthal–Oblong numbers:
     *     Oₙ = (1/9) · (2ⁿ – (–1)ⁿ) · (2ⁿ⁺¹ – (–1)ⁿ⁺¹)
     */
    fun closedForm(n: Int): BigInteger {
        val a = BigInteger.ONE.shl(n)
        val b = bigIntSgn(n)
        val c = a.shl(1)
        val d = -b
        return (a - b) * (c - d) / BigInteger.valueOf(9)
    }
}

/**
 * Jacobsthal number closed form:
 *     Jₙ = (2ⁿ – (–1)ⁿ) / 3
 */
private object JacobsthalClosedForm {
    private val THREE: BigInteger = BigInteger.valueOf(3L)
    private val cache = memoize<Int, BigInteger> { n ->
        (BigInteger.ONE.shl(n) - bigIntSgn(n)) / THREE
    }

    operator fun invoke(n: Int): BigInteger = cache(n)
}
