package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood
import org.vorpal.kosmos.functional.datastructures.Either


/**
 * A simple finite undirected graph with vertex type [V].
 *
 * Semantics:
 *  - The vertex set is given by [vertices] (a [FiniteSet], no duplicates).
 *  - The edge set [edges] is a [FiniteSet] of unordered pairs `{u, v}` with `u ≠ v`.
 *  - No loops: edges `{v, v}` are forbidden.
 *  - No parallel edges: since [edges] is a [FiniteSet], there is at most one
 *    edge between any unordered pair `{u, v}`.
 *
 * Algebraic flavour:
 *  - Together with [overlay] and [edgeless] (defined at the companion object level),
 *    undirected graphs on a fixed vertex set form a commutative [Monoid] (overlay semiring "addition").
 *  - Together with [disjointUnion] and [empty] (defined at the companion object level),
 *    *all* finite undirected graphs up to relabelling form a commutative [Monoid] under disjoint union.
 *
 * The [Neighborhood] implementation uses the standard notion of adjacency:
 * [neighbors] of a vertex returns all vertices joined to it by an edge.
 */
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
     * Returns the disjoint union of this graph `G` and another graph `H`.
     *
     * The disjoint union `G ⊔ H` places the two graphs side-by-side as separate
     * connected components, with no edges between them and with their vertices
     * kept distinct even if they share the same underlying type.
     *
     * Vertex set:
     *  - The vertices of `G ⊔ H` are tagged with `Either` to remember
     *    which graph they came from:
     *
     *      - `Either.Left(v)` for each `v ∈ V(G)`
     *      - `Either.Right(w)` for each `w ∈ V(H)`
     *
     *   Formally:
     *
     *   `V(G ⊔ H) = { Left(v)  : v ∈ V(G) } ∪ { Right(w) : w ∈ V(H) }`.
     *
     * Edge set:
     *  - Each edge of `G` becomes an edge between `Left`-vertices:
     *      `{u, v} ∈ E(G)` ↦ `{Left(u), Left(v)} ∈ E(G ⊔ H)`.
     *  - Each edge of `H` becomes an edge between `Right`-vertices:
     *      `{x, y} ∈ E(H)` ↦ `{Right(x), Right(y)} ∈ E(G ⊔ H)`.
     *  - No edges are created between `Left` and `Right` vertices.
     *
     * This construction is the categorical coproduct of graphs, with the empty
     * graph playing the role of the unit object.
     */
    infix fun <W: Any> disjointUnion(other: UndirectedGraph<W>): UndirectedGraph<Either<V, W>>

    /**
     * Returns the join of this graph `G` with another graph `H`.
     *
     * The join `G ⋈ H` is obtained by:
     *  1. Taking the overlay (edgewise union) of `G` and `H`, and
     *  2. Adding all possible edges between vertices of `G` and vertices of `H`.
     *
     * Vertices:
     *  - The vertex set of the result is the union of the vertex sets:
     *    `V = V(G) ∪ V(H)`, represented by `Either<V, W>`.
     *
     * Edges:
     *  - All edges of `G` and all edges of `H` are present.
     *  - Additionally, for every `g ∈ V(G)` and `h ∈ V(H)`, the edge `{g, h}` is added
     *    in the form `{Left(g), Right(h)}`.
     *
     * If the vertex sets of `G` and `H` are disjoint, this agrees with the usual
     * graph-theoretic definition of the join as the disjoint union plus all edges
     * between the two parts.
     */
    infix fun <W: Any> join(other: UndirectedGraph<W>): UndirectedGraph<Either<V, W>>

    /**
     * Returns the overlay (edgewise union) of this graph and [other].
     *
     * Vertices:
     *  - The vertex set of the result is the union of the vertex sets:
     *    `V = V(this) ∪ V(other)`.
     *
     * Edges:
     *  - The edge set of the result is the union of the edge sets:
     *    `E = E(this) ∪ E(other)`.
     *  - If an edge appears in both graphs, it appears only once in the result.
     *
     * Intuitively, this "stacks" the two graphs on top of each other over the same
     * vertex universe.
     *
     * If the vertex sets are disjoint, this is isomorphic to the
     * disjoint union of the two graphs with the same vertex labels.
     */
    infix fun overlay(other: UndirectedGraph<V>): UndirectedGraph<V>

    /**
     * Constructs `G × H` (alternatively written `G □ H`),
     * the Cartesian product of this graph `G` and another graph `H`.
     *
     * The vertex set is the Cartesian product of the vertex sets:
     * `V(G × H) = V(G) × V(H)`.
     *
     * There is an (undirected) edge `{(u1, v1), (u2, v2)} ∈ E(G × H)` iff either:
     *  1. `u1 = u2` and `{v1, v2} ∈ E(H)`, or
     *  2. `{u1, u2} ∈ E(G)` and `v1 = v2`.
     */
    infix fun <W: Any> cartesianProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>>

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
