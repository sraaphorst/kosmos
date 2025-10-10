package org.vorpal.kosmos.combinatorial.recurrence

/**
 * A two-parameter recurrence-defined combinatorial array (e.g. Pascal, Stirling, Lah).
 */
interface BivariateRecurrence<T> {
    operator fun invoke(n: Int, k: Int): T

    /** Row n as sequence over k = 0..n. */
    fun row(n: Int): Sequence<T> = (0..n).asSequence().map { k -> invoke(n, k) }

    /** Column k as sequence over n = k, k+1, ... */
    fun column(k: Int): Sequence<T> = generateSequence(k) { it + 1 }.map { n -> invoke(n, k) }
}
