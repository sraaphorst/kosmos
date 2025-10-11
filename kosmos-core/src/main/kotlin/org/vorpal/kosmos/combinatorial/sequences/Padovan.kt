package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.CachedLinearSequence
import java.math.BigInteger

/**
 * Padovan's sequence.
 *
 * Here are some combinatorial interpretations of this sequence.
 *
 * 1. P(n) is the number of ways of writing n + 2 as an ordered sum in which each term is either 2 or 3
 * (i.e. the number of compositions of n + 2 in which each term is either 2 or 3).
 * For example, P(6) = 4, and there are 4 ways to write 8 as an ordered sum of 2s and 3s:
 *       - 2 + 2 + 2 + 2  ; 2 + 3 + 3  ; 3 + 2 + 3  ; 3 + 3 + 2
 *
 * 2. The number of ways of writing n as an ordered sum in which no term is 2 is P(2n − 2).
 * For example, P(6) = 4, and there are 4 ways to write 4 as an ordered sum in which no term is 2:
 *       - 4  ; 1 + 3  ; 3 + 1  ; 1 + 1 + 1 + 1
 *
 * 3. The number of ways of writing n as a palindromic ordered sum in which no term is 2 is P(n).
 * For example, P(6) = 4, and there are 4 ways to write 6 as a palindromic ordered sum in which no term is 2:
 *       - 6  ; 3 + 3  ; 1 + 4 + 1  ; 1 + 1 + 1 + 1 + 1 + 1
 *
 * 4. The number of ways of writing n as an ordered sum in which each term is odd and greater than 1 is equal to P(n − 5).
 * For example, P(6) = 4, and there are 4 ways to write 11 as an ordered sum in which each term is odd and greater than 1:
 *       - 11 ; 5 + 3 + 3 ; 3 + 5 + 3 ; 3 + 3 + 5
 *
 * 5. The number of ways of writing n as an ordered sum in which each term is congruent to 2 mod 3 is equal to P(n − 4).
 * For example, P(6) = 4, and there are 4 ways to write 10 as an ordered sum in which each term is congruent to 2 mod 3:
 *       - 8 + 2  ; 2 + 8  ; 5 + 5  ; 2 + 2 + 2 + 2 + 2
 *
 * There are multiple ways to write this as a linear recurrence.
 *
 * In [Padovan1], we write it as P_n = P_{n-2} + P_{n-3}, with seeds:
 * - P_0 = P_1 = P_2 = 1.
 *
 * Its characteristic polynomial is `x³ - x - 1 = 0`.
 *
 * The closed form uses the real root `ρ` (plastic constant):
 *
 * ```
 * Pₙ = A·ρⁿ + B·αⁿ + C·βⁿ
 * ```
 */
object Padovan1 :
    CachedLinearSequence(
        initial = listOf(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
        coefficients = listOf(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE)
)

/**
 * See [Padovan1] for a more thorough definition.
 *
 * In [Padovan2], we write this recurrence as:
 * - P_n = P_{n-1} + P_{n-5} with seeds:
 * - P_0 = P_1 = P_2 = 1
 * - P_3 = P_4 = 2.
 */
object Padovan2 :
    CachedLinearSequence(
        initial = listOf(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.TWO, BigInteger.TWO),
        coefficients = listOf(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE)
)
