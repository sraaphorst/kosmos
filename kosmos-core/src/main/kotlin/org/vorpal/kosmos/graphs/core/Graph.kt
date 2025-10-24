package org.vorpal.kosmos.graphs.core

import org.vorpal.kosmos.core.FiniteSet

/**
 * The most basic form of a finite graph:
 * - A finite set of vertices
 * - A finite set of edges
 * These can be defined over whatever type one desires.
 */
interface Graph<V, E> {
    val vertices: FiniteSet<V>
    val edges: FiniteSet<E>
}
