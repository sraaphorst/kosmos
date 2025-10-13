package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger

/**
 * **Euler numbers** E_n (secant numbers with alternating signs) — OEIS A000364.
 *
 * Generating function:
 *     sec x = Σ_{n≥0} E_{2n} x^{2n} / (2n)!
 * and E_{2n+1} = 0 (for n ≥ 0), E₀ = 1, E₂ = −1, E₄ = 5, E₆ = −61, …
 *
 * Relationship to Euler zigzag numbers A(n) (OEIS A000111):
 *     |E_{2n}| = A(2n),   and   E_{2n} = (−1)^n · A(2n)
 *     E_{2n+1} = 0
 */
object EulerNumbers :
    CachedRecurrence<BigInteger> by EulerRecurrence

private object EulerRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n == 0 -> BigInteger.ONE
        n % 2 == 1 -> BigInteger.ZERO
        else -> bigIntSgn(n / 2) * EulerZigzag(n)
    }
}
