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

    fun inducedSubgraph(subvertices: FiniteSet<V>): UndirectedGraph<V>

    /**
     * Create the line graph of an undirected graph.
     */
    fun toLineGraph(): UndirectedGraph<UndirectedEdge<V>>

    /**
     * Take the complement graph, i.e. the graph that contains edge e in VxV if:
     * 1. e is not a loop; and
     * 2. e is not in this graph.
     */
    fun toComplementGraph(): UndirectedGraph<V>

    /**
     * Create a directed representation of this graph where each undirected edge is replaced by two perpendicular
     * directed edges.
     */
    fun toDirectedGraph(): DirectedGraph<V>

    /**
     * Constructs `G × H`, the Cartesian product of this graph `G` and another graph `H`.
     *
     * The vertex set is the Cartesian product of the vertex sets:
     * `V(G × H) = V(G) × V(H)`.
     *
     * There is an (undirected) edge `{(u1, v1), (u2, v2)} ∈ E(G × H)` iff either:
     *  1. `u1 = u2` and `{v1, v2} ∈ E(H)`, or
     *  2. `{u1, u2} ∈ E(G)` and `v1 = v2`.
     */
    fun <W: Any> cartesianProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>>

    /**
     * Functorial map on the vertex type of this undirected graph.
     * Applies [f] to every vertex, and transports each edge `{u, v}`
     * to an edge `{f(u), f(v)}`.
     */
    fun <W: Any> mapVertices(f: (V) -> W): AdjacencySetUndirectedGraph<W>

    /**
     * Take the vertices of the graph (with no order guaranteed) and remap them
     * to the set `{0, ..., v-1}`.
     *
     * The function returns the new graph (over [Int]) and the map from the
     * integers to the original vertices.
     */
    fun canonicalizeVertices(): Pair<UndirectedGraph<Int>, Map<Int, V>>
}

data class UndirectedEdge<V: Any>(val u: V, val v: V): Edge<V> {
    init {
        require(u != v) { "Loops not allowed in undirected simple graphs: $u"}
    }
    override fun equals(other: Any?): Boolean =
        other is UndirectedEdge<V> && u in other && v in other

    /**
     * Determine if two edges are incident to each other, i.e. they share a vertex.
     */
    infix fun incidentTo(other: UndirectedEdge<V>): Boolean =
        this != other && (this.u in other || this.v in other)

    /**
     * True iff these edges can appear consecutively in some walk of the undirected graph.
     * This is just an alias for [incidentTo].
     */
    infix fun composesWith(other: UndirectedEdge<V>): Boolean =
        incidentTo(other)

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
