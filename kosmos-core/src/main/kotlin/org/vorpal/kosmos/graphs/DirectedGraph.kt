package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood
import org.vorpal.kosmos.functional.datastructures.Either


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
     * Disjoint union `G ⊔ H` of this directed graph `G` with another directed graph `H`.
     *
     * Vertices:
     *  - every vertex `v ∈ V(G)` becomes `Either.Left(v)`,
     *  - every vertex `w ∈ V(H)` becomes `Either.Right(w)`.
     *
     * Formally:
     *   `V(G ⊔ H) = { Left(v) : v ∈ V(G) } ∪ { Right(w) : w ∈ V(H) }`.
     *
     * Edges:
     *  - every arc `(u, v) ∈ E(G)` becomes `(Left(u), Left(v))`,
     *  - every arc `(x, y) ∈ E(H)` becomes `(Right(x), Right(y))`,
     *  - no edges between the `Left` and `Right` parts are added.
     *
     * This is the categorical coproduct of `G` and `H` in the category of directed graphs,
     * made explicit in the type via `Either<V, W>`.
     */
    infix fun <W: Any> disjointUnion(other: DirectedGraph<W>): DirectedGraph<Either<V, W>>

    /**
     * Join `G + H` (also written `G ∇ H`, `G ⋈ H`) of this directed graph `G` with another directed graph `H`,
     * on a disjoint vertex universe.
     *
     * It is defined as the disjoint union `G ⊔ H` (using `Either<V, W>` to tag vertices),
     * plus additional "cross edges" between the two parts.
     *
     * Vertex set:
     *   - `V(G ⋈ H) = V(G ⊔ H) = { Left(v) : v ∈ V(G) } ∪ { Right(w) : w ∈ V(H) }`.
     *
     * Edges:
     *   - all edges of `G`, relabelled into the `Left` part:
     *       `(u, v) ∈ E(G)  ↦  (Left(u), Left(v))`,
     *   - all edges of `H`, relabelled into the `Right` part:
     *       `(x, y) ∈ E(H)  ↦  (Right(x), Right(y))`,
     *   - plus, for each pair `(v, w)` with `v ∈ V(G)`, `w ∈ V(H)`,
     *     cross edges as follows:
     *
     *       joinIn (this function):
     *         adds all arcs `Left(v) → Right(w)`
     *
     *       joinOut:
     *         adds all arcs `Right(w) → Left(v)`
     *
     *       join:
     *         adds arcs in both directions:
     *           `Left(v) → Right(w)` and `Right(w) → Left(v)`.
     *
     * This matches the usual graph-theoretic "join of disjoint graphs", but made
     * type-safe by tagging vertices with `Either<V, W>`. Vertices from `G` and `H`
     * are always treated as distinct, even if the underlying values are equal.
     */
    infix fun <W: Any> joinIn(other: DirectedGraph<W>): DirectedGraph<Either<V, W>>

    /**
     * Join `G + H` (also written `G ∇ H`, `G ⋈ H`) of this directed graph `G` with another directed graph `H`,
     * on a disjoint vertex universe.
     *
     * It is defined as the disjoint union `G ⊔ H` (using `Either<V, W>` to tag vertices),
     * plus additional "cross edges" between the two parts.
     *
     * Vertex set:
     *   - `V(G ⋈ H) = V(G ⊔ H) = { Left(v) : v ∈ V(G) } ∪ { Right(w) : w ∈ V(H) }`.
     *
     * Edges:
     *   - all edges of `G`, relabelled into the `Left` part:
     *       `(u, v) ∈ E(G)  ↦  (Left(u), Left(v))`,
     *   - all edges of `H`, relabelled into the `Right` part:
     *       `(x, y) ∈ E(H)  ↦  (Right(x), Right(y))`,
     *   - plus, for each pair `(v, w)` with `v ∈ V(G)`, `w ∈ V(H)`,
     *     cross edges as follows:
     *
     *       joinIn:
     *         adds all arcs `Left(v) → Right(w)`
     *
     *       joinOut (this function):
     *         adds all arcs `Right(w) → Left(v)`
     *
     *       join:
     *         adds arcs in both directions:
     *           `Left(v) → Right(w)` and `Right(w) → Left(v)`.
     *
     * This matches the usual graph-theoretic "join of disjoint graphs", but made
     * type-safe by tagging vertices with `Either<V, W>`. Vertices from `G` and `H`
     * are always treated as distinct, even if the underlying values are equal.
     */
    infix fun <W: Any> joinOut(other: DirectedGraph<W>): DirectedGraph<Either<V, W>>

    /**
     * Join `G + H` (also written `G ∇ H`, `G ⋈ H`) of this directed graph `G` with another directed graph `H`,
     * on a disjoint vertex universe.
     *
     * It is defined as the disjoint union `G ⊔ H` (using `Either<V, W>` to tag vertices),
     * plus additional "cross edges" between the two parts.
     *
     * Vertex set:
     *   - `V(G ⋈ H) = V(G ⊔ H) = { Left(v) : v ∈ V(G) } ∪ { Right(w) : w ∈ V(H) }`.
     *
     * Edges:
     *   - all edges of `G`, relabelled into the `Left` part:
     *       `(u, v) ∈ E(G)  ↦  (Left(u), Left(v))`,
     *   - all edges of `H`, relabelled into the `Right` part:
     *       `(x, y) ∈ E(H)  ↦  (Right(x), Right(y))`,
     *   - plus, for each pair `(v, w)` with `v ∈ V(G)`, `w ∈ V(H)`,
     *     cross edges as follows:
     *
     *       joinIn:
     *         adds all arcs `Left(v) → Right(w)`
     *
     *       joinOut:
     *         adds all arcs `Right(w) → Left(v)`
     *
     *       join (this function):
     *         adds arcs in both directions:
     *           `Left(v) → Right(w)` and `Right(w) → Left(v)`.
     *
     * This matches the usual graph-theoretic "join of disjoint graphs", but made
     * type-safe by tagging vertices with `Either<V, W>`. Vertices from `G` and `H`
     * are always treated as distinct, even if the underlying values are equal.
     */
    infix fun <W: Any> join(other: DirectedGraph<W>): DirectedGraph<Either<V, W>>

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
    infix fun overlay(other: DirectedGraph<V>): DirectedGraph<V>

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

