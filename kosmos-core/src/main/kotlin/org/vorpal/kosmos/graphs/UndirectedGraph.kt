package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood

sealed interface UndirectedGraph<V: Any>: Graph<V>, Neighborhood<V> {
    override fun neighbors(of: V): FiniteSet.Unordered<V> =
        edges
            .filter { of in it }
            .map { if (it.u == of) it.v else it.u }
            .toUnordered()

    val edges: FiniteSet.Unordered<UndirectedEdge<V>>

    fun degree(v: V): Int = neighbors(v).size
    fun hasEdge(u: V, v: V): Boolean = v in neighbors(u)
}

data class UndirectedEdge<V: Any>(val u: V, val v: V): Edge<V> {
    init {
        require(u != v) { "Loops not allowed in undirected simple graphs: $u}"}
    }
    override fun equals(other: Any?): Boolean =
        other is UndirectedEdge<V> && u in other && v in other

    /**
     * For this edge {u, v}, given a vertex v, get the other vertex u in it.
     */
    fun otherEnd(vertex: V): V {
        require(vertex in this) {
            "Vertex $vertex not in edge $this."
        }
        return if (vertex == u) v else u
    }

    override fun hashCode(): Int =
        u.hashCode() xor v.hashCode()

    override operator fun contains(vertex: V): Boolean =
        vertex == u || vertex == v

    /**
     * Convert this [UndirectedEdge] into a [DirectedEdge] starting at [from] and going to [to].
     */
    fun toDirectedEdge(from: V): DirectedEdge<V> {
        require(from in this) { "Vertex $from not in edge $this." }
        return when(from) {
            u -> DirectedEdge(u, v)
            v -> DirectedEdge(v, from)
            else -> throw IllegalArgumentException("Vertex $from not in edge $this.")
        }
    }

    /**
     * Convert this [UndirectedEdge] into a [Pair] of [DirectedEdge]s in both directions.
     */
    fun toDirectedEdges(): Pair<DirectedEdge<V>, DirectedEdge<V>> =
        toDirectedEdge(u) to toDirectedEdge(v)
}
