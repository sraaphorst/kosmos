package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * An abstraction of a general graph, containing a set of vertices.
 */
sealed interface Graph<V: Any> {
    val vertices: FiniteSet.Unordered<V>
    val order: Int
        get() = vertices.size
}

/**
 * Vertices that arise from the disjoint sum of two graphs.
 */
sealed interface SumVertex<A, B> {
    data class InLeft<A, B>(val value: A) : SumVertex<A, B>
    data class InRight<A, B>(val value: B) : SumVertex<A, B>
}
