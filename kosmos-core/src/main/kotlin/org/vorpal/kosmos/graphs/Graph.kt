package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet

/**
 * An abstraction of a general graph, containing a set of vertices.
 */
sealed interface Graph<V: Any> {
    val vertices: FiniteSet.Unordered<V>
    val order: Int
        get() = vertices.size
}

