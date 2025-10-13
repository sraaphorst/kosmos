package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import java.math.BigInteger

/**
 * **Labeled hypergraphs** Hₙ — OEIS A152457.
 *
 * Number of distinct undirected hypergraphs on n labeled vertices.
 *
 * Closed form:
 * ```
 * Hₙ = 2^{(2ⁿ − n − 1)}
 * ```
 *
 * Each of the 2ⁿ possible vertex subsets can independently
 * be included or excluded as a hyperedge, except:
 * - we exclude the empty set
 * - we exclude singletons (size 1)
 *
 * Examples:
 * ```
 * n : 0, 1, 2, 3, 4, 5
 * H : 1, 1, 2, 8, 128, 32768
 * ```
 */
object Hypergraphs :
    CachedClosedForm<BigInteger> by HypergraphsClosedForm

private object HypergraphsClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        when (n) {
            0 -> BigInteger.ONE
            else -> BigInteger.TWO.pow((1 shl n) - n - 1)
        }
}
