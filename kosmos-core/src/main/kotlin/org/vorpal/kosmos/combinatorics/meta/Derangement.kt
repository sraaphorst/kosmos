package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Factorial
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Derangement numbers** (or **subfactorials**) — the number of permutations of n objects
 * with no fixed points.
 *
 * Recurrence:
 * ```
 * !0 = 1
 * !1 = 0
 * !n = (n−1)(!(n−1) + !(n−2))
 * ```
 *
 * Closed form:
 * ```
 * !n = n! · Σₖ₌₀ⁿ (−1)ᵏ / k!
 * ```
 *
 * First few terms:
 * ```
 * 1, 0, 1, 2, 9, 44, 265, 1854, 14833, ...
 * ```
 *
 * OEIS A000166
 */
object Derangement :
    CachedRecurrence<BigInteger> by DerangementRecurrence,
    CachedClosedForm<BigInteger> by DerangementClosedForm

private object DerangementRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        1 -> BigInteger.ZERO
        else -> (n - 1).toBigInteger() * (this(n - 1) + this(n - 2))
    }
}

private object DerangementClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger = when (n) {
        0 -> BigInteger.ONE
        1 -> BigInteger.ZERO
        else -> (0..n).fold(BigInteger.ZERO) { acc, k ->
            acc + bigIntSgn(k) * Factorial(n) / Factorial(k)
        }
    }
}
