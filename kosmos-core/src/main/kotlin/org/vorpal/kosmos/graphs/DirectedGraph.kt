package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood

sealed interface DirectedGraph<V: Any>: Graph<V>, Neighborhood<V> {
    val edges: FiniteSet.Unordered<DirectedEdge<V>>

    fun outEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        edges.filter { it.from == of }.toUnordered()

    fun outNeighbors(of: V): FiniteSet.Unordered<V> =
       outEdges(of).map { it.to }.toUnordered()

    override fun neighbors(of: V): FiniteSet.Unordered<V> =
        outNeighbors(of)

    fun outDegree(v: V): Int = outNeighbors(v).size

    fun inEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        edges.filter { it.to == of }.toUnordered()

    fun inNeighbors(of: V): FiniteSet.Unordered<V> =
        inEdges(of).map { it.from }.toUnordered()

    fun inDegree(v: V): Int = inNeighbors(v).size

    fun hasArc(from: V, to: V): Boolean = to in outNeighbors(from)

    fun allNeighbors(of: V): FiniteSet.Unordered<V> =
        (outNeighbors(of) + inNeighbors(of)).toUnordered()
}

data class DirectedEdge<V: Any>(val from: V, val to: V): Edge<V> {
    init {
        require(from != to) { "Loops not allowed in this directed graph representation: ($from. $to)" }
    }

    override fun contains(vertex: V): Boolean = from == vertex || to == vertex

    /**
     * Convert this [DirectedEdge] into an [UndirectedEdge] by eliminating the concept of direction.
     */
    fun toUndirectedEdge(): UndirectedEdge<V> =
        UndirectedEdge(from, to)
}

