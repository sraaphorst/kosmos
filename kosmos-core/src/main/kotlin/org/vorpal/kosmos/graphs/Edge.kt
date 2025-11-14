package org.vorpal.kosmos.graphs

/**
 * An abstract representation of an edge connecting vertices in a graph or hypergraph.
 *
 * An edge is a fundamental component of graph structures, representing a connection
 * or relationship between vertices. This interface provides a minimal contract that
 * all edge types must satisfy, whether directed, undirected, weighted, or part of
 * a hypergraph.
 *
 * ## Type Parameters
 * @param V The type of vertices that this edge connects. Must be non-nullable ([Any])
 *          to ensure well-defined equality and hashing.
 *
 * ## Design Notes
 * This is a sealed interface to allow for controlled inheritance. The primary
 * implementations are:
 * - [DirectedEdge]: An ordered pair `(u, v)` representing `u → v`
 * - [UndirectedEdge]: An unordered pair `{u, v}` representing `u ↔ v` (alt: `u ~ v`)
 *
 * The interface supports destructuring via [component1] and [component2], allowing
 * edges to be used in destructuring declarations:
 * ```kotlin
 * val edge: UndirectedEdge<Int> = UndirectedEdge(1, 2)
 * val (u, v) = edge  // u = 1, v = 2
 * ```
 *
 * @see DirectedEdge
 * @see UndirectedEdge
 */
sealed interface Edge<V: Any> {
    fun contains(vertex: V): Boolean

    operator fun component1(): V
    operator fun component2(): V
}
