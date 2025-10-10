package org.vorpal.kosmos.combinatorial.recurrence

/**
 * A one-parameter function that has a closed form of computation.
 * We limit to Int in this case because we want to do numerical comparisons.
 */
fun interface ClosedForm<T> {
    fun closedForm(n: Int): T
}
