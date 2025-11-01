package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.frameworks.sequence.CachedLinearRecurrenceImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import java.math.BigInteger

/**
 * **Padovan sequence** P(n).
 *
 * ### Combinatorial interpretations
 *
 * 1. *Compositions of n + 2 into parts 2 or 3.*
 *    Example: P(6) = 4 counts the 4 ways to write 8 as 2s and 3s:
 *    2+2+2+2, 2+3+3, 3+2+3, 3+3+2.
 *
 * 2. *Compositions of n with no term equal to 2* → P(2n − 2).
 *
 * 3. *Palindromic compositions of n with no term equal to 2* → P(n).
 *
 * 4. *Compositions of n into odd parts > 1* → P(n − 5).
 *
 * 5. *Compositions of n into parts ≡ 2 mod 3* → P(n − 4).
 *
 * ### Linear recurrence form
 * ```
 * P₀ = P₁ = P₂ = 1
 * Pₙ = Pₙ₋₂ + Pₙ₋₃
 * ```
 *
 * Characteristic polynomial:
 * ```
 * x³ − x − 1 = 0
 * ```
 *
 * Closed form:
 * ```
 * Pₙ = A·ρⁿ + B·αⁿ + C·βⁿ
 * ```
 * where ρ ≈ 1.3247… is the plastic constant.
 */
object Padovan1 :
    CachedRecurrence<BigInteger> by Padovan1Recurrence

private object Padovan1Recurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
    selectors = listOf(-2, -3),
    coefficients = listOf(1, 1),
    constantTerm = BigInteger.ZERO,
    multiply = Action { s, t -> s.toBigInteger() * t },
    add = BinOp(BigInteger::add)
)

/**
 * **Alternative Padovan formulation.**
 *
 * See [Padovan1] for a more thorough definition.
 *
 * ```
 * P₀ = P₁ = P₂ = 1
 * P₃ = P₄ = 2
 * Pₙ = Pₙ₋₁ + Pₙ₋₅
 * ```
 */
object Padovan2 :
    CachedRecurrence<BigInteger> by Padovan2Recurrence

private object Padovan2Recurrence : CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.TWO, BigInteger.TWO),
    selectors = listOf(-1, -5),
    coefficients = listOf(1, 1),
    constantTerm = BigInteger.ZERO,
    multiply = Action({ s, t -> s.toBigInteger() * t }),
    add = BinOp(BigInteger::add)
)