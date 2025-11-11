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

    fun inducedSubgraph(subvertices: FiniteSet<V>): DirectedGraph<V>

    /**
     * Create the line graph of this directed graph.
     */
    fun toLineGraph(): DirectedGraph<DirectedEdge<V>>

    /**
     * Create the complement of this directed graph, i.e. for every (u, v) in V x V with u ≠ v, if (u, v)
     * is not in this graph, then (u, v) is in the complement graph.
     */
    fun toComplementGraph(): DirectedGraph<V>

    /**
     * Transpose this graph, i.e. turn all the edges around.
     */
    fun toTransposeGraph(): DirectedGraph<V>

    /**
     * Turn this directed graph into an undirected graph by replacing each edge with an undirected edge.
     */
    fun toUndirectedGraph(): UndirectedGraph<V>

    /**
     * Constructs `G × H` (alternatively written `G □ H`),
     * the Cartesian product of this graph `G` and another graph `H`.
     *
     * The vertex set is the Cartesian product of the vertex sets:
     *
     * `V(G × H) = V(G) × V(H)`.
     *
     * There is a directed edge `((u1, v1), (u2, v2)) ∈ E(G × H)` iff either:
     *  1. `u1 = u2` and `(v1, v2) ∈ E(H)`, or
     *  2. `(u1, u2) ∈ E(G)` and `v1 = v2`.
     */
    infix fun <W: Any> cartesianProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>>

    /**
     * Functorial map on the vertex type of this undirected graph.
     * Applies [f] to every vertex, and transports each edge `{u, v}`
     * to an edge `{f(u), f(v)}`.
     */
    fun <W : Any> mapVertices(f: (V) -> W): DirectedGraph<W>

    /**
     * Take the vertices of the graph (with no order guaranteed) and remap them
     * to the set `{0, ..., v-1}`.
     *
     * The function returns the new graph (over [Int]) and the map from the
     * integers to the original vertices.
     */
    fun canonicalizeVertices(): Pair<DirectedGraph<Int>, Map<Int, V>>
}


data class DirectedEdge<V: Any>(val from: V, val to: V): Edge<V> {
    init {
        require(from != to) { "Loops not allowed in this directed graph representation: ($from, $to)" }
    }

    /**
     * True iff this edge can be followed immediately by [other] in a directed walk:
     * (from -> to) andThen (other.from -> other.to) is defined.
     *
     * This mirrors the semantics of function/lens `andThen`: do this, then [other].
     */
    infix fun canAndThen(other: DirectedEdge<V>): Boolean =
        this.to == other.from

    /**
     * A more “categorical” alias that is synonymous with [canAndThen], i.e. this edge can be followed
     * immediately by [other] in a directed walk:
     * (u -> v) composesWith (v -> w)
     */
    infix fun composesWith(other: DirectedEdge<V>): Boolean =
        canAndThen(other)

    /**
     * True iff this edge can be followed immediately by [other] in a directed walk:
     * (u -> v) leadsTo (v -> w).
     */
    infix fun andThen(other: DirectedEdge<V>): DirectedEdge<V> =
        DirectedEdge(this.from, other.to)

    /**
     * A more “categorical” alias that is synonymous with [andThen], i.e. this edge can be followed
     * immediately by [other] in a directed walk:
     * (u -> v) composableWith (v -> w)
     */
    infix fun compose(other: DirectedEdge<V>): DirectedEdge<V> =
        other andThen this

    override fun contains(vertex: V): Boolean = from == vertex || to == vertex

    /**
     * Reverse this edge's direction:
     * If this directed edge is (u -> v), this operation returns the directed edge (v -> u).
     */
    fun reverse(): DirectedEdge<V> =
        DirectedEdge(to, from)

    /**
     * Convert this [DirectedEdge] into an [UndirectedEdge] by eliminating the concept of direction.
     */
    fun toUndirectedEdge(): UndirectedEdge<V> =
        UndirectedEdge(from, to)
}

