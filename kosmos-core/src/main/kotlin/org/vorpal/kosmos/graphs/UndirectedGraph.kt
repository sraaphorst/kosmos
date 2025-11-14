package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.functional.datastructures.Option

/**
 * A simple finite undirected graph with vertex type [V].
 *
 * Semantics:
 *  - The vertex set is given by [vertices] (a finite set, no duplicates).
 *  - The edge set [edges] is a finite set of unordered pairs `{u, v}` with `u ≠ v`.
 *  - No loops: edges `{v, v}` are forbidden.
 *  - No parallel edges: since [edges] is a [FiniteSet], there is at most one
 *    edge between any unordered pair `{u, v}`.
 *
 * Algebraic flavor:
 *  - Together with [overlay] and [edgeless], undirected graphs on a fixed
 *    vertex set form a commutative monoid (overlay semiring "addition").
 *  - Together with [disjointUnion] and [empty], *all* finite undirected graphs
 *    up to relabelling form a commutative monoid under disjoint union.
 *
 * The [Neighborhood] implementation uses the standard notion of adjacency:
 * [neighbors] of a vertex returns all vertices joined to it by an edge.
 */
sealed interface UndirectedGraph<V: Any>: Graph<V>, Neighborhood<V> {
    val edges: FiniteSet.Unordered<UndirectedEdge<V>>

    val isEmpty: Boolean
        get() = vertices.isEmpty
    val isNotEmpty: Boolean
        get() = vertices.isNotEmpty

    /**
     * Returns the (undirected) degree of vertex [v]:
     * the number of neighbors adjacent to [v] in this graph.
     *
     * Formally, `deg(v) = |{ u ∈ V : {u, v} ∈ E }|`.
     */
    fun degree(v: V): Int = neighbors(v).size

    /**
     * Returns the minimum degree among all vertices of this graph,
     * or [Option.None] if the graph has no vertices.
     *
     * Formally, for non-empty `V`,
     * `minDegree() = min { deg(v) : v ∈ V }`.
     */
    fun minDegree(): Option<Int> =
        Option.of(vertices.minOfOrNull(::degree))

    /**
     * Returns the maximum degree among all vertices of this graph,
     * or [Option.None] if the graph has no vertices.
     *
     * Formally, for non-empty `V`,
     * `maxDegree() = max { deg(v) : v ∈ V }`.
     */
    fun maxDegree(): Option<Int> =
        Option.of(vertices.maxOfOrNull(::degree))

    /**
     * Returns `true` iff this graph is *k-regular*.
     *
     * A graph is k-regular if every vertex has degree exactly `k`.
     *
     * By convention here:
     *  - The empty graph is considered 0-regular.
     *  - A non-empty graph is 0-regular iff it has no edges
     *    (all vertices have degree 0).
     */
    fun isKRegular(k: Int): Boolean =
        if (vertices.isEmpty) k == 0
        else vertices.all { degree(it) == k }

    /**
     * Returns `true` iff this graph is regular:
     * all vertices have the same degree.
     *
     * The empty graph is considered regular (vacuously).
     * For a non-empty graph this is equivalent to saying
     * that there exists some `k` such that the graph is k-regular.
     */
    fun isRegular(): Boolean {
        if (isEmpty) return true
        val d0 = degree(vertices.first())
        return vertices.all { degree(it) == d0 }
    }

    /**
     * Returns the set of all vertices `{v | {of, v} ∈ E}`.
     */
    override fun neighbors(of: V): FiniteSet.Unordered<V> =
        edges
            .filter { of in it }
            .map { if (it.u == of) it.v else it.u }
            .toUnordered()

    /**
     * Returns `true` iff this graph has no edges.
     *
     * Equivalently, every vertex has degree 0,
     * or the complement graph is the complete graph.
     */
    fun isEdgeless(): Boolean = edges.isEmpty

    /**
     * Returns `true` iff this graph is a complete graph on its vertex set.
     *
     * For a simple undirected graph on `n = order` vertices,
     * a complete graph `K_n` has exactly `n(n - 1) / 2` edges.
     * Since this representation forbids loops and parallel edges,
     * having this many edges is equivalent to every unordered pair
     * of distinct vertices being joined by an edge.
     */
    fun isComplete(): Boolean = edges.size == order * (order - 1) / 2

    /**
     * Returns `true` iff this graph is connected:
     * it has exactly one connected component.
     *
     * The empty graph is considered disconnected here
     * (it has zero components).
     */
    fun isConnected(): Boolean = connectedComponents().size == 1

    /**
     * Returns `true` iff there is an edge between [u] and [v].
     *
     * Since the graph is undirected, this tests whether `{u, v} ∈ E`.
     */
    fun hasEdge(u: V, v: V): Boolean = v in neighbors(u)

    /**
     * Returns the degree sequence of this graph as a [List] of degrees.
     *
     * The degrees are listed in the [FiniteSet.Ordered] view of [vertices]:
     * the order is deterministic for a given [FiniteSet] but is not
     * necessarily sorted by degree or by vertex value.
     */
    fun degreeSequence(): List<Int> = vertices.toOrdered().map(::degree).toList()

    /**
     * The [degreeSequence] of this graph, sorted in non-ascending order.
     *
     * This is a classic graph isomorphism invariant.
     */
    fun sortedDegreeSequence(): List<Int> = degreeSequence().sortedDescending()

    /**
     * Returns `true` iff this graph contains at least one cycle.
     *
     * Characterization: a finite undirected graph with vertex set `V`,
     * edge set `E`, and `c` connected components is acyclic (a forest)
     * iff `|E| = |V| - c`.
     *
     * Therefore, this graph has a cycle iff:
     *
     *    `|E| > |V| - (#components)`.
     */
    fun hasCycle(): Boolean = edges.size > vertices.size - connectedComponents().size

    /**
     * Returns `true` iff this graph is a tree:
     * a connected, acyclic undirected graph.
     *
     * Equivalently, a tree on `n` vertices has exactly `n - 1` edges
     * and a single connected component.
     */
    fun isTree(): Boolean = connectedComponents().size == 1 && edges.size == vertices.size - 1

    /**
     * Returns `true` iff this graph is a forest:
     * an acyclic undirected graph (possibly disconnected).
     *
     * For a forest with vertex set `V`, edge set `E`, and `c` connected
     * components we have `|E| = |V| - c`.
     *
     * This predicate checks that identity.
     */
    fun isForest(): Boolean = edges.size == vertices.size - connectedComponents().size

    /**
     * Given a subset of vertices `W`, calculate the subgraph of this graph on `W`.
     * The edges are:
     *
     *     E ∩ (W × W)
     *
     */
    fun inducedSubgraph(subvertices: FiniteSet<V>): UndirectedGraph<V>

    /**
     * Decompose this undirected graph into its (weakly) connected component vertex sets.
     *
     * Two vertices u and v lie in the same component iff there is an undirected
     * path between them.
     * The result is a finite set of vertices per connected component, one for
     * each equivalence class of vertices under this relation.
     *
     * Note: the results of the initial call to this method are cached,
     * so calling this method multiple times does not entail additional computation.
     */
    fun connectedComponentsVertexSets(): FiniteSet<FiniteSet<V>>

    /**
     * Decompose this undirected graph into its (weakly) connected components.
     *
     * Two vertices u and v lie in the same component iff there is an undirected
     * path between them.
     * The result is a finite set of induced subgraphs, one for
     * each equivalence class of vertices under this relation.
     *
     * Note: the results of the initial call to this method are cached,
     * so calling this method multiple times does not entail additional computation.
     */
    fun connectedComponents(): FiniteSet<UndirectedGraph<V>>

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
     * Returns `true` iff this undirected graph has an Eulerian **path** (a trail
     * that uses every edge exactly once, not necessarily closed).
     *
     * Criterion:
     *  - Let `core = inducedSubgraph({ v | degree(v) > 0 })` (drop isolated vertices).
     *  - `core` must be connected; and
     *  - In `core`, either:
     *      • all vertices have even degree (then a circuit exists, hence a path), or
     *      • exactly two vertices have odd degree (the unique possible start/end).
     *
     * This predicate implements exactly that check.
     *
     * Notes:
     *  - If there are no edges at all, the result is `true` by convention.
     *
     * Complexity: O(|V| + |E|), assuming `connectedComponents()` is O(|V| + |E|).
     */
    fun hasEulerianPath(): Boolean = eulerian(allowOpen = true)

    /**
     * Returns `true` iff this undirected graph has an Eulerian **circuit** (a closed trail
     * that uses every edge exactly once).
     *
     * Criterion (finite simple undirected graphs):
     *  - Let `core = inducedSubgraph({ v | degree(v) > 0 })` (drop isolated vertices).
     *  - Either `core` is empty (trivially Eulerian), or:
     *      (1) `core` is connected; and
     *      (2) every vertex in `core` has even degree.
     *
     * This predicate implements exactly that check.
     *
     * Complexity: O(|V| + |E|), assuming `connectedComponents()` is O(|V| + |E|).
     */
    fun hasEulerianCircuit(): Boolean = eulerian(allowOpen = false)

    private fun eulerian(allowOpen: Boolean): Boolean {
        val nonIsolated = vertices.filter { degree(it) > 0 }
        if (nonIsolated.isEmpty) return true
        val core = inducedSubgraph(nonIsolated)
        if (core.connectedComponents().size != 1) return false
        val oddCount = core.vertices.count { core.degree(it) % 2 != 0 }
        return if (allowOpen) oddCount == 0 || oddCount == 2
        else oddCount == 0
    }

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

/**
 * A simple undirected edge `{u, v}` with `u ≠ v`.
 *
 * Invariants:
 *  - [u] and [v] are distinct; loops `{v, v}` are not allowed.
 *  - Equality and hashing are independent of orientation:
 *
 *    `{u, v} == {v, u}` and hash codes are symmetric.
 *
 * This makes [UndirectedEdge] behave like a 2-element subset of V rather than
 * an ordered pair.
 *
 * Algebraic intuition:
 *  - You can think of {u, v} as a generator for the free undirected graph
 *    on two vertices, with [incidentTo] and [composesWith] describing how
 *    such generators can be chained in walks.
 */
data class UndirectedEdge<V: Any>(val u: V, val v: V): Edge<V> {
    init {
        require(u != v) { "Loops not allowed in undirected simple graphs: $u"}
    }
    override fun equals(other: Any?): Boolean =
        other is UndirectedEdge<V> && u in other && v in other

    /**
     * True iff these edges are incident, i.e. they share a vertex and are not equal.
     * In other words, there exists a vertex x such that x ∈ this ∩ other.
     *
     * This is the basic relation used when forming the line graph L(G).
     */
    infix fun incidentTo(other: UndirectedEdge<V>): Boolean =
        this != other && (this.u in other || this.v in other)

    /**
     * True iff these edges can appear consecutively in some undirected walk.
     *
     * Alias for [incidentTo], but phrased compositionally:
     * we can "compose" two edges when they meet at a common vertex.
     */
    infix fun composesWith(other: UndirectedEdge<V>): Boolean =
        incidentTo(other)

    /**
     * For this edge `{u, v}`, given one endpoint [vertex], return the other endpoint.
     *
     * @throws IllegalArgumentException if [vertex] is not an endpoint of this edge.
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
