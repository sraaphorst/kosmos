package org.vorpal.kosmos.frameworks.array

/**
 * A two-parameter function that has a closed form of computation.
 * We limit to Int in this case because we want to do numerical comparisons.
 */
fun interface BivariateClosedForm<T> {
    fun closedForm(n: Int, k: Int): T
}
