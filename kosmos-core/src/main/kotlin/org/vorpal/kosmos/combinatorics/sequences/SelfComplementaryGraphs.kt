package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import java.math.BigInteger

/**
 * **Self-complementary graphs** — OEIS A000171.
 *
 * Number of *labeled graphs* that are isomorphic to their complement.
 *
 * Defined only for n ≡ 0 or 1 (mod 4):
 * ```
 * Sₙ = 2^{n(n−1)/4},   if n ≡ 0 or 1 mod 4
 * Sₙ = 0,              otherwise
 * ```
 *
 * These graphs are perfectly symmetric under complement inversion.
 * They are a key test case for automorphism group algorithms.
 *
 * Examples:
 * ```
 * n : 1, 2, 3, 4, 5, 6, 7, 8
 * S : 1, 0, 0, 2, 16, 0, 0, 1024
 * ```
 */
object SelfComplementaryGraphs :
    CachedClosedForm<BigInteger> by SelfComplementaryClosedForm

private object SelfComplementaryClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger = when {
        n % 4 == 0 || n % 4 == 1 -> BigInteger.TWO.pow(n * (n - 1) / 4)
        else -> BigInteger.ZERO
    }
}
