package org.vorpal.kosmos.frameworks.array

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * A two-parameter recurrence-defined combinatorics array (e.g. Pascal, Stirling, Lah).
 */
interface BivariateRecurrence {
    fun createCache() = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    operator fun invoke(n: Int, k: Int): BigInteger

    /** Row n as sequence over k = 0..n. */
    fun row(n: Int): Sequence<BigInteger> = (0..n).asSequence().map { k -> invoke(n, k) }

    /** Column k as sequence over n = k, k+1, ... */
    fun column(k: Int): Sequence<BigInteger> = generateSequence(k) { it + 1 }.map { n -> invoke(n, k) }
}
