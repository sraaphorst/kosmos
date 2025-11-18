package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.neighborhood.Neighborhood
import org.vorpal.kosmos.functional.datastructures.Either


/**
 * A simple finite directed graph (digraph) with vertex type [V].
 *
 * Semantics:
 *  - The vertex set is [vertices] (finite, no duplicates).
 *  - The edge set [edges] is a finite set of directed edges (u → v).
 *  - No loops: edges (v → v) are forbidden.
 *  - No parallel arcs: at most one edge (u → v) for a given ordered pair.
 *
 * Provided views:
 *  - [outNeighbors] / [outEdges]: vertices/edges reachable by one outgoing arc.
 *  - [inNeighbors] / [inEdges]: vertices/edges that have an arc into a vertex.
 *  - [neighbors] (from [Neighborhood]) is defined as [outNeighbors], so plain
 *    BFS/DFS follow the direction of edges.
 *  - [allNeighbors] unions in- and out-neighbors, giving the "underlying"
 *    undirected adjacency used for weak connectivity.
 *
 * Algebraic flavor:
 *  - Together with [overlay] and [edgeless], digraphs on a fixed vertex set
 *    form a commutative monoid under overlay.
 *  - Together with [disjointUnion] and [empty], all finite digraphs up to
 *    relabelling form a commutative monoid under disjoint union.
 *  - [cartesianProduct] gives the standard graph-theoretic Cartesian product
 *    G □ H on digraphs.
 */
sealed interface DirectedGraph<V: Any>: Graph<V>, Neighborhood<V> {
    val edges: FiniteSet.Unordered<DirectedEdge<V>>

    fun outEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        edges.filter { it.from == of }.toUnordered()

    fun outNeighbors(of: V): FiniteSet.Unordered<V> =
       outEdges(of).map { it.to }.toUnordered()

    override fun neighbors(of: V): FiniteSet.Unordered<V> =
        outNeighbors(of)

    /**
     * All neighbors (undirected view):
     * the union of [outNeighbors] and [inNeighbors] of [of].
     *
     * Useful for weak connectivity/traversals that ignore arc direction.
     */
    fun allNeighbors(of: V): FiniteSet.Unordered<V> =
        (outNeighbors(of) union inNeighbors(of)).toUnordered()

    fun outDegree(v: V): Int = outNeighbors(v).size

    fun inEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        edges.filter { it.to == of }.toUnordered()

    fun inNeighbors(of: V): FiniteSet.Unordered<V> =
        inEdges(of).map { it.from }.toUnordered()

    fun inDegree(v: V): Int = inNeighbors(v).size

    fun sources(): FiniteSet<V> =
        vertices.filter { inNeighbors(it).isEmpty }.toUnordered()

    fun sinks(): FiniteSet<V> =
        vertices.filter { outNeighbors(it).isEmpty }.toUnordered()

    fun hasArc(from: V, to: V): Boolean = to in outNeighbors(from)

    fun inducedSubgraph(subvertices: FiniteSet<V>): DirectedGraph<V>

    /**
     * Weakly connected components as vertex sets.
     *
     * Two vertices lie in the same weak component iff they are connected in the
     * underlying undirected graph (i.e., ignoring arc directions).
     *
     * Returns a finite set of disjoint vertex sets whose union is `V`.
     *
     * Complexity (typical implementation): O(|V| + |E|).
     */
    fun weaklyConnectedComponentsVertexSets(): FiniteSet<FiniteSet<V>>

    /**
     * Weakly connected components as induced subgraphs of this digraph.
     *
     * Each component is the subgraph induced by one of the sets returned by
     * [weaklyConnectedComponentsVertexSets]. Edges keep their original directions.
     *
     * Complexity (build-time): O(|V| + |E|) to find components; materializing
     * induced subgraphs is linear in total size.
     */
    fun weaklyConnectedComponents(): FiniteSet<DirectedGraph<V>>

    /**
     * Strongly connected components as vertex sets.
     *
     * Two vertices are in the same set iff each is reachable from the other.
     * (Equivalence classes of the mutual-reachability relation.)
     *
     * Typical implementation via Kosaraju/Tarjan/Gabow runs in O(|V| + |E|).
     */
    fun stronglyConnectedComponentsVertexSets(): FiniteSet<FiniteSet<V>>

    /**
     * Strongly connected components as induced subgraphs of this digraph.
     *
     * Each returned graph is the subgraph induced by one SCC’s vertex set.
     * The condensation DAG of `G` is formed by contracting each returned subgraph
     * to a single vertex.
     */
    fun stronglyConnectedComponents(): FiniteSet<DirectedGraph<V>>

    /**
     * Line graph `L(G)` of a directed graph `G`.
     *
     * Vertices of `L(G)` are the arcs of `G`. There is an arc
     * `e1 → e2` in `L(G)` iff `e1.to == e2.from` in `G`
     * (i.e., the two edges can be followed consecutively).
     *
     * Complexity: `O(|E|^2)` in the worst case (dense overlap of endpoints).
     */
    fun toLineGraph(): DirectedGraph<DirectedEdge<V>>

    /**
     * Directed complement of this simple directed graph.
     *
     * On the same vertex set, includes every edge `u → v` with `u ≠ v`
     * that does *not* appear in this graph.
     *
     * Loops remain forbidden.
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
     * Functorial map on the vertex type of this directed graph.
     * Applies [f] to each vertex and transports each arc `(u, v)` to `(f(u), f(v))`.
     * Edge multiplicity and simplicity are preserved by the [FiniteSet] representation.
     */
    fun <W : Any> mapVertices(f: (V) -> W): DirectedGraph<W>

    /**
     * Relabel vertices by a dense index set `{0, …, n−1}`.
     *
     * Returns the relabelled graph and a dictionary from indices back to original vertices.
     * The ordering is derived from [FiniteSet.toOrdered] and is deterministic for a given set.
     */
    fun canonicalizeVertices(): Pair<DirectedGraph<Int>, Map<Int, V>>
}


/**
 * A simple directed edge (arc) from [from] to [to] with [from] ≠ [to].
 *
 * Invariants:
 *  - No loops: `(v → v)` is not allowed.
 *  - Equality and hashing are those of an ordered pair `(from, to)`.
 *
 * Algebraic intuition:
 *  - Composition of edges corresponds to composition of morphisms:
 *    `(u → v)` can compose with `(v → w)`, but not with `(x → y)` when `v ≠ x`.
 */
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
     * True iff this edge can be followed immediately by [other] in a directed walk:
     * (u -> v) leadsTo (v -> w).
     */
    infix fun andThen(other: DirectedEdge<V>): DirectedEdge<V> =
        DirectedEdge(this.from, other.to)


    /**
     * A more “categorical” alias that is synonymous with [canAndThen].
     *
     * In categorical terms, we can compose `(u → v)` with `(v → w)` to get a
     * composite arrow `(u → w)`.
     *
     * Here we only check that the middle vertex matches.
     */
    infix fun composesWith(other: DirectedEdge<V>): Boolean =
        other canAndThen this

    /**
     * Categorical composition: this ∘ other.
     *
     * Applies [other] first, then this, i.e. requires `other.to == this.from`
     * and yields the composite `(other.from → this.to)`.
     *
     * Equivalent to `other andThen this`.
     */
    infix fun compose(other: DirectedEdge<V>): DirectedEdge<V> =
        other andThen this

    override fun contains(vertex: V): Boolean = from == vertex || to == vertex

    /**
     * Reverse the direction of this edge.
     *
     * If this edge is `(u → v)`, this returns `(v → u)`.
     * This is exactly the edge you see in the transpose graph `Gᵗ`.
     */
    fun reverse(): DirectedEdge<V> =
        DirectedEdge(to, from)

    /**
     * Convert this [DirectedEdge] into an [UndirectedEdge] by eliminating the concept of direction.
     */
    fun toUndirectedEdge(): UndirectedEdge<V> =
        UndirectedEdge(from, to)
}

