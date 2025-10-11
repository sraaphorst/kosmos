package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.Factorial
import org.vorpal.kosmos.combinatorial.recurrence.CachedNonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.CachedClosedForm
import java.math.BigInteger

/**
 * **Catalan numbers** Cₙ:
 * number of Dyck paths, binary trees, or correct bracket expressions with n pairs.
 *
 * Recurrence (nonlinear):
 * ```
 * C₀ = 1
 * Cₙ₊₁ = Σₖ₌₀ⁿ Cₖ · Cₙ₋ₖ
 * ```
 *
 * Closed form:
 * ```
 * Cₙ = (1 / (n + 1)) · (2n choose n)
 *     = (2n)! / ((n + 1)! · n!)
 * ```
 *
 * OEIS A000108
 */
object Catalan :
    CachedNonlinearRecurrence(
        initial = listOf(BigInteger.ONE),
        next = { prefix ->
            val n = prefix.lastIndex
            (0..n).fold(BigInteger.ZERO) { acc, i ->
                acc + prefix[i] * prefix[n - i]
            }
        }
    ),
    CachedClosedForm {
    override fun closedFormCalculator(n: Int): BigInteger =
        Factorial(2 * n) / (Factorial(n + 1) * Factorial(n))
}