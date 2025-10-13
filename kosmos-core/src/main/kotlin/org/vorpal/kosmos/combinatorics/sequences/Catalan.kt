package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.Factorial
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Catalan numbers** Cₙ —
 * the number of Dyck paths, binary trees, or well-formed bracket expressions with *n* pairs.
 *
 * ### Recurrence (nonlinear)
 * ```
 * C₀ = 1
 * Cₙ = Σₖ₌₀ⁿ⁻¹ Cₖ · Cₙ₋₁₋ₖ
 * ```
 *
 * ### Closed form
 * ```
 * Cₙ = (1 / (n + 1)) · (2n choose n)
 *     = (2n)! / ((n + 1)! · n!)
 * ```
 *
 * OEIS A000108
 */
object Catalan :
    CachedRecurrence<BigInteger> by CatalanRecurrence,
    CachedClosedForm<BigInteger> by CatalanClosedForm

private object CatalanRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        else -> (0 until n / 2).fold(BigInteger.ZERO) { acc, i ->
            val term = this(i) * this(n - 1 - i)
            if (2 * i == n - 1) acc + term else acc + term * BigInteger.TWO
        }
    }
}

private object CatalanClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        Factorial(2 * n) / (Factorial(n + 1) * Factorial(n))
}