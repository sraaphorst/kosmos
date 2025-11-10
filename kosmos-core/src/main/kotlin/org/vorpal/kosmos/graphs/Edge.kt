package org.vorpal.kosmos.graphs

/**
 * The concept of an edge, which contains vertices.
 * This can be used for [Graph] or hypergraph, and directed or undirected edges.
 */
sealed interface Edge<V: Any> {
    fun contains(vertex: V): Boolean

    operator fun component1(): V
    operator fun component2(): V
}
