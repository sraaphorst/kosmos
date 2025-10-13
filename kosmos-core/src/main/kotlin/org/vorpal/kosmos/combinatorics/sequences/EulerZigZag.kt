package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.arrays.Entringer
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Euler zigzag (up–down) numbers** A(n) — OEIS A000111.
 *
 * Counts alternating permutations of {1,…,n}.
 * Computed here as the row sum of the Entringer triangle:
 *     A(n) = Σ_{k=0}^{n} Entringer(n, k)
 *
 * First values:
 * 1, 1, 1, 2, 5, 16, 61, 272, 1385, 7936, …
 *
 * Notes:
 * - Even indices relate to (the absolute values of) **Euler numbers** (secant numbers).
 * - Odd indices relate to **tangent numbers**.
 */
object EulerZigzag :
    CachedRecurrence<BigInteger> by object : CachedRecurrenceImplementation<BigInteger>() {
        override fun recursiveCalculator(n: Int): BigInteger =
            (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Entringer(n, k) }
    }
