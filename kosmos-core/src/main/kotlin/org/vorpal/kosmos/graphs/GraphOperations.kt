package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.functional.datastructures.swap

/**
 * Vertex types for the corona product of two graphs.
 * A vertex is either from the graph on the left,
 * or from its copy of the graph on the right.
 */
typealias CoronaVertex<V, W> = Either<V, Pair<V, W>>

/**
 * Constructs the corona product G ⊙ H of this undirected graph G with another
 * undirected graph H.
 *
 * Vertices:
 *  - one "center" vertex for each vertex v ∈ V(G), and
 *  - for each v ∈ V(G), one "satellite" vertex (v, w) for every w ∈ V(H).
 *
 * Formally:
 * kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *
 *
 * Edges:
 *  1. All edges of G between center vertices.
 *  2. For each v ∈ V(G), a copy of H on the satellites { {v, w} : w ∈ V(H) }.
 *  3. For each v ∈ V(G) and w ∈ V(H), an edge between the center v and its satellite {v, w}.
 */
fun <V: Any, W: Any> AdjacencySetUndirectedGraph<V>.coronaProduct(
    other: UndirectedGraph<W>
): AdjacencySetUndirectedGraph<CoronaVertex<V, W>> =
    coronaProductImpl(
        vertices,
        edges,
        other.vertices,
        other.edges,
        { u, v -> UndirectedEdge(u, v) },
        { vertices, edges -> AdjacencySetUndirectedGraph.of(vertices, edges) },
        isDirected = false,
        satelliteIn = true,
        satelliteOut = true
    )

/**
 * Constructs the corona product G ⊙ H of this directed graph G with another
 * directed graph H.
 *
 * Vertices:
 *  - one "center" vertex for each vertex v ∈ V(G), and
 *  - for each v ∈ V(G), one "satellite" vertex (v, w) for every w ∈ V(H).
 *
 * Formally:
 * kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *
 *
 * Edges:
 *  1. All edges of G between center vertices.
 *  2. For each v ∈ V(G), a copy of H on the satellites { (v, w) : w ∈ V(H) }.
 *  3. For each v ∈ V(G) and w ∈ V(H), **an edge to the center from its satellite (w, v).**
 */
fun <V: Any, W: Any> AdjacencySetDirectedGraph<V>.coronaProductIn(
    other: DirectedGraph<W>
): AdjacencySetDirectedGraph<CoronaVertex<V, W>> =
    coronaProductImpl(
        vertices,
        edges,
        other.vertices,
        other.edges,
        { u, v -> DirectedEdge(u, v) },
        { vertices, edges -> AdjacencySetDirectedGraph.of(vertices, edges) },
        isDirected = true,
        satelliteIn = true,
        satelliteOut = false
    )

/**
 * Constructs the corona product G ⊙ H of this directed graph G with another
 * directed graph H.
 *
 * Vertices:
 *  - one "center" vertex for each vertex v ∈ V(G), and
 *  - for each v ∈ V(G), one "satellite" vertex (v, w) for every w ∈ V(H).
 *
 * Formally:
 * kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *
 *
 * Edges:
 *  1. All edges of G between center vertices.
 *  2. For each v ∈ V(G), a copy of H on the satellites { (v, w) : w ∈ V(H) }.
 *  3. For each v ∈ V(G) and w ∈ V(H), **an edge from the center to its satellite (v, w).**
 */
fun <V: Any, W: Any> AdjacencySetDirectedGraph<V>.coronaProductOut(
    other: DirectedGraph<W>
): AdjacencySetDirectedGraph<CoronaVertex<V, W>> =
    coronaProductImpl(
        vertices,
        edges,
        other.vertices,
        other.edges,
        { u, v -> DirectedEdge(u, v) },
        { vertices, edges -> AdjacencySetDirectedGraph.of(vertices, edges) },
        isDirected = true,
        satelliteIn = false,
        satelliteOut = true
    )

/**
 * Constructs the corona product G ⊙ H of this directed graph G with another
 * directed graph H.
 *
 * Vertices:
 *  - one "center" vertex for each vertex v ∈ V(G), and
 *  - for each v ∈ V(G), one "satellite" vertex (v, w) for every w ∈ V(H).
 *
 * Formally:
 * kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *
 *
 * Edges:
 *  1. All edges of G between center vertices.
 *  2. For each v ∈ V(G), a copy of H on the satellites { (v, w) : w ∈ V(H) }.
 *  3. For each v ∈ V(G) and w ∈ V(H), **edges between the center and its satellite (v, w), (w, v).**
 */
fun <V: Any, W: Any> AdjacencySetDirectedGraph<V>.coronaProductBidirectional(
    other: DirectedGraph<W>
): AdjacencySetDirectedGraph<CoronaVertex<V, W>> =
    coronaProductImpl(
        vertices,
        edges,
        other.vertices,
        other.edges,
        { u, v -> DirectedEdge(u, v) },
        { vertices, edges -> AdjacencySetDirectedGraph.of(vertices, edges) },
        isDirected = true,
        satelliteIn = true,
        satelliteOut = true
    )

/**
 * Constructs the corona product G ⊙ H of this undirected graph G with another
 * undirected graph H.
 *
 * Vertices:
 *  - one "center" vertex for each vertex v ∈ V(G), and
 *  - for each v ∈ V(G), one "satellite" vertex (v, w) for every w ∈ V(H).
 *
 * Formally:
 * kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *
 *
 * Edges:
 *  1. All edges of G between center vertices.
 *  2. For each v ∈ V(G), a copy of H on the satellites { (v, w) : w ∈ V(H) }.
 *  3. For each v ∈ V(G) and w ∈ V(H), an edge between the center v and its satellite (v, w).
 *
 *  There is a lot of casting in here, but that is because there is an issue with
 */
private fun <V : Any, W : Any, EV : Edge<V>, EW : Edge<W>, E : Edge<CoronaVertex<V, W>>, G> coronaProductImpl(
    vertices: FiniteSet<V>,
    edges: FiniteSet<EV>,
    otherVertices: FiniteSet<W>,
    otherEdges: FiniteSet<EW>,
    createEdge: (CoronaVertex<V, W>, CoronaVertex<V, W>) -> E,
    buildGraph: (FiniteSet.Unordered<CoronaVertex<V, W>>, FiniteSet.Unordered<E>) -> G,
    isDirected: Boolean,
    satelliteIn: Boolean,
    satelliteOut: Boolean
): G {
    val centerVertices: FiniteSet<CoronaVertex<V, W>> =
        vertices.map { v -> CoronaVertex.left(v) }.toUnorderedFiniteSet()

    val satelliteVertices: FiniteSet<CoronaVertex<V, W>> =
        vertices.flatMap { v ->
            otherVertices.map { w -> CoronaVertex.right(v to w) }
        }.toUnorderedFiniteSet()

    val centerEdges: List<E> =
        edges.toList().map { (u, v) ->
            createEdge(
                CoronaVertex.left(u),
                CoronaVertex.left(v)
            )
        }

    val satelliteEdges: List<E> =
        vertices.toList().flatMap { v ->
            otherEdges.toList().map { (w1, w2) ->
                createEdge(
                    CoronaVertex.right(v to w1),
                    CoronaVertex.right(v to w2)
                )
            }
        }

    val satelliteInEdges: List<E> =
        if (!isDirected || satelliteIn) {
            vertices.toList().flatMap { v ->
                otherVertices.toList().map { w ->
                    createEdge(
                        CoronaVertex.left(v),
                        CoronaVertex.right(v to w)
                    )
                }
            }
        } else emptyList()

    val satelliteOutEdges =
        if (isDirected && satelliteOut) {
            vertices.toList().flatMap { v ->
                otherVertices.toList().map { w ->
                    createEdge(
                        CoronaVertex.right(v to w),
                        CoronaVertex.left(v)
                    )
                }
            }
        } else emptyList()

    val allVertices = (centerVertices + satelliteVertices).toUnorderedFiniteSet()
    val allEdges = buildList {
        addAll(centerEdges)
        addAll(satelliteEdges)
        addAll(satelliteInEdges)
        addAll(satelliteOutEdges)
    }.toUnorderedFiniteSet()

    return buildGraph(allVertices, allEdges)
}

/* =========================================================
 *  Side swap for Either-vertex graphs (isomorphisms)
 * ========================================================= */

fun <V : Any, W : Any> AdjacencySetDirectedGraph<Either<V, W>>.swapSides():
        AdjacencySetDirectedGraph<Either<W, V>> =
    mapVertices { it.swap() }

fun <V : Any, W : Any> AdjacencySetUndirectedGraph<Either<V, W>>.swapSides():
        AdjacencySetUndirectedGraph<Either<W, V>> =
    mapVertices { it.swap() }

/* =========================================================
 *  Meet / Subgraph-of / Connect
 * ========================================================= */

/** Greatest lower bound in subgraph order: V = V₁ ∩ V₂; E = E₁ ∩ E₂ restricted to V. */
infix fun <V : Any> UndirectedGraph<V>.meet(other: UndirectedGraph<V>): UndirectedGraph<V> {
    val v = (this.vertices intersect other.vertices).toUnorderedFiniteSet()
    val e = (this.edges intersect other.edges).filter { e -> e.u in v && e.v in v }.toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(v, e)
}

/** Directed meet under subgraph order. */
infix fun <V : Any> DirectedGraph<V>.meet(other: DirectedGraph<V>): DirectedGraph<V> {
    val v = (this.vertices intersect other.vertices).toUnorderedFiniteSet()
    val e = (this.edges intersect other.edges).filter { e -> e.from in v && e.to in v }.toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(v, e)
}

/** Meet on a fixed [universe] (both coerced to the same carrier). */
fun <V : Any> UndirectedGraph<V>.meetOn(universe: FiniteSet<V>, other: UndirectedGraph<V>): UndirectedGraph<V> {
    val gx = this.embedIntoUniverse(universe)
    val gy = other.embedIntoUniverse(universe)
    val e = (gx.edges intersect gy.edges).toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(universe.toUnorderedFiniteSet(), e)
}

fun <V : Any> DirectedGraph<V>.meetOn(universe: FiniteSet<V>, other: DirectedGraph<V>): DirectedGraph<V> {
    val gx = this.embedIntoUniverse(universe)
    val gy = other.embedIntoUniverse(universe)
    val e = (gx.edges intersect gy.edges).toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(universe.toUnorderedFiniteSet(), e)
}

/** Subgraph relation. */
infix fun <V : Any> UndirectedGraph<V>.isSubgraphOf(other: UndirectedGraph<V>): Boolean =
    this.vertices.all { it in other.vertices } && this.edges.all { it in other.edges }

infix fun <V : Any> DirectedGraph<V>.isSubgraphOf(other: DirectedGraph<V>): Boolean =
    this.vertices.all { it in other.vertices } && this.edges.all { it in other.edges }

/** Undirected connect (⋈): add all cross edges {u,v} with u∈V(G), v∈V(H), u≠v. */
infix fun <V : Any> UndirectedGraph<V>.connect(other: UndirectedGraph<V>): UndirectedGraph<V> {
    val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
    val cross = this.vertices.flatMap { u ->
        other.vertices.mapNotNull { w -> if (u != w) UndirectedEdge(u, w) else null }
    }.toUnorderedFiniteSet()
    val e = (this.edges + other.edges + cross).toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(v, e)
}

/** Directed connect: add all cross arcs u→v with u∈V(G), v∈V(H), u≠v. */
infix fun <V : Any> DirectedGraph<V>.connect(other: DirectedGraph<V>): DirectedGraph<V> {
    val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
    val cross = this.vertices.flatMap { u ->
        other.vertices.mapNotNull { w -> if (u != w) DirectedEdge(u, w) else null }
    }.toUnorderedFiniteSet()
    val e = (this.edges + other.edges + cross).toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(v, e)
}

/* =========================================================
 *  Products
 * ========================================================= */

/** Tensor/direct product ⊗ (undirected). */
infix fun <V : Any, W : Any> UndirectedGraph<V>.tensorProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val e = v.flatMap { (u1, v1) ->
        v.mapNotNull { (u2, v2) ->
            if (u1 != u2 || v1 != v2) {
                val e1 = UndirectedEdge(u1, u2) in this.edges
                val e2 = UndirectedEdge(v1, v2) in other.edges
                if (e1 && e2) UndirectedEdge(u1 to v1, u2 to v2) else null
            } else null
        }
    }.toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(v, e)
}

/** Tensor/direct/Kronecker product ⊗ (directed). */
infix fun <V : Any, W : Any> DirectedGraph<V>.tensorProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val e = edges.flatMap { (a, b) -> other.edges.map { (c, d) -> DirectedEdge(a to c, b to d) } }.toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(v, e)
}

/** Strong product ⊠ (undirected). */
infix fun <V : Any, W : Any> UndirectedGraph<V>.strongProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val e = v.flatMap { (u1, v1) ->
        v.mapNotNull { (u2, v2) ->
            if (u1 != u2 || v1 != v2) {
                val gEdge = UndirectedEdge(u1, u2) in this.edges
                val hEdge = UndirectedEdge(v1, v2) in other.edges
                if ((gEdge && v1 == v2) || (hEdge && u1 == u2) || (gEdge && hEdge))
                    UndirectedEdge(u1 to v1, u2 to v2) else null
            } else null
        }
    }.toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(v, e)
}

/** Strong product ⊠ (directed) = □ (cartesian) ∪ ⊗ (tensor). */
infix fun <V : Any, W : Any> DirectedGraph<V>.strongProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val cart = buildList {
        other.edges.forEach { (c, d) -> vertices.forEach { a -> add(DirectedEdge(a to c, a to d)) } }
        edges.forEach { (a, b) -> other.vertices.forEach { c -> add(DirectedEdge(a to c, b to c)) } }
    }
    val tens = edges.flatMap { (a, b) -> other.edges.map { (c, d) -> DirectedEdge(a to c, b to d) } }
    val e = (cart + tens).toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(v, e)
}

/** Lexicographic product (undirected). */
infix fun <V : Any, W : Any> UndirectedGraph<V>.lexicographicProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val e = v.flatMap { (u1, v1) ->
        v.mapNotNull { (u2, v2) ->
            if (u1 != u2 || v1 != v2) {
                val gEdge = UndirectedEdge(u1, u2) in this.edges
                val hEdge = UndirectedEdge(v1, v2) in other.edges
                if (gEdge || (u1 == u2 && hEdge)) UndirectedEdge(u1 to v1, u2 to v2) else null
            } else null
        }
    }.toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(v, e)
}

/** Lexicographic product (directed). */
infix fun <V : Any, W : Any> DirectedGraph<V>.lexicographicProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
    val v = vertices.cartesianProduct(other.vertices).toUnordered()
    val across = edges.flatMap { (a, b) ->
        other.vertices.flatMap { c -> other.vertices.map { d -> DirectedEdge(a to c, b to d) } }
    }
    val within = vertices.flatMap { a -> other.edges.map { (c, d) -> DirectedEdge(a to c, a to d) } }
    val e = (across + within).toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(v, e)
}

/**
 * Builds the *d-th power* of this undirected graph, commonly denoted `G^d`.
 *
 * Two distinct vertices `u` and `v` are adjacent in `G^d` iff their shortest-path
 * distance in the original graph is at most `d`.
 *
 * Formally:
 *
 *  - `V(G^d) = V(G)`,
 *  - `E(G^d) = { {u,v} : u ≠ v and dist_G(u, v) ≤ d }`.
 *
 * The resulting graph is simple:
 *  - no self-loops are added (pairs with `u == v` are ignored),
 *  - parallel edges are deduplicated by the underlying `FiniteSet`.
 *
 * Special cases and behavior:
 *  - `d == 0` ⇒ edgeless graph on `V(G)`.
 *  - `d == 1` ⇒ exactly the original graph.
 *  - Disconnected inputs are handled component-wise: only vertices within the
 *    same connected component can become adjacent.
 *
 * Implementation notes:
 *  - Uses a depth-limited BFS from each vertex (cut off at depth `d`). To avoid
 *    generating each undirected edge twice, vertices are given stable indices and
 *    we only add an edge `{u,v}` the first time we encounter it (when `idx(v) > idx(u)`).
 *
 * Complexity:
 *  - Time: `O( Σ_v BFS_d(v) )`, i.e. the sum of depth-limited BFS costs. In the worst
 *    case this is `O(|V|(|V|+|E|))`, but typically much smaller for moderate `d`.
 *  - Space: `O(|V| + |E|)` transiently (queue/visited), plus the output edge set.
 *
 * @receiver The base undirected graph `G`.
 * @param d Non-negative radius used to define adjacency in `G^d`.
 * @return A new [AdjacencySetUndirectedGraph] on the same vertex set representing `G^d`.
 * @throws IllegalArgumentException if `d < 0`.
 *
 * @see distancesFrom for the underlying metric used
 * @see bfsDepthLimitedFrom for the traversal primitive
 */
fun <V: Any> UndirectedGraph<V>.power(d: Int): AdjacencySetUndirectedGraph<V> {
    require(d >= 0) { "Power must be non-negative: $d" }
    if (vertices.isEmpty || d == 0) return AdjacencySetUndirectedGraph.edgeless(vertices)

    // Give each vertex a stable index so we only add edges once (idx[w] > idx[v]).
    val idx = vertices.toList().withIndex().associate { (i, v) -> v to i }

    val acc = mutableListOf<UndirectedEdge<V>>()
    for (v in vertices) {
        val iv = idx.getValue(v)
        // reuse the depth-limited BFS helper
        bfsDepthLimitedFrom(
            start = v,
            neighbors = { x -> neighbors(x) },
            maxDepth = d,
            initial = Unit,
            onDiscover = { _, w, _ ->
                if (w !== v && idx.getValue(w) > iv) acc += UndirectedEdge(v, w)
            },
            onEdge = { s, _, _, _, _ -> s }
        )
    }
    return AdjacencySetUndirectedGraph.of(vertices, acc.toUnorderedFiniteSet())
}

/**
 * Builds the *out-power* `G^d_out` of this directed graph.
 *
 * For vertices `u` and `v`, the arc `u → v` is present in the result iff there exists
 * a directed path in the original graph from `u` to `v` of length at most `d`
 * following **out-edges only**. Formally,
 *
 *  - `V(G^d_out) = V(G)`,
 *  - `E(G^d_out) = { (u,v) : u ≠ v and dist_G^→(u, v) ≤ d }`,
 *
 * where `dist_G^→` is the shortest directed (forward) path length. No self-loops are
 * created (`u == v` is excluded). The operation is *not* symmetric: in general
 * `(u,v)` in `G^d_out` does **not** imply `(v,u)` in `G^d_out`, even when both are within
 * distance `≤ d` in the underlying undirected sense.
 *
 * Special cases:
 *  - `d == 0` ⇒ edgeless digraph on `V(G)`.
 *  - `d == 1` ⇒ exactly the original digraph.
 *  - Strong connectivity is *not* required: if `v` is unreachable from `u`, no arc is added.
 *
 * Implementation notes:
 *  - Performs a depth-limited BFS from each source `u` using `outNeighbors`. Every
 *    discovered vertex `w ≠ u` within depth `≤ d` receives an arc `u → w`.
 *
 * Complexity:
 *  - Time: `O( Σ_u BFS_d^→(u) )`, i.e. the sum of depth-limited forward BFS costs.
 *    Worst-case `O(|V|(|V|+|E|))`.
 *  - Space: `O(|V| + |E|)` transiently (queue/visited), plus output arcs.
 *
 * @receiver The base directed graph `G`.
 * @param d Non-negative radius in terms of directed path length.
 * @return A new `AdjacencySetDirectedGraph` whose arcs capture reachability within
 *         `≤ d` steps along out-edges.
 * @throws IllegalArgumentException if `d < 0`.
 *
 * @see DirectedGraph.outNeighbors for the direction respected by the metric
 * @see bfsDepthLimitedFrom for the traversal primitive
 * @see weakDistancesFrom for the undirected (weak) alternative
 */
fun <V: Any> DirectedGraph<V>.powerOut(d: Int): AdjacencySetDirectedGraph<V> {
    require(d >= 0) { "Power must be non-negative: $d" }
    if (vertices.isEmpty || d == 0) return AdjacencySetDirectedGraph.edgeless(vertices)

    val acc = mutableListOf<DirectedEdge<V>>()
    for (v in vertices) {
        bfsDepthLimitedFrom(
            start = v,
            neighbors = { x -> outNeighbors(x) },
            maxDepth = d,
            initial = Unit,
            onDiscover = { _, w, _ ->
                if (w !== v) acc += DirectedEdge(v, w)
            },
            onEdge = { s, _, _, _, _ -> s }
        )
    }
    return AdjacencySetDirectedGraph.of(vertices, acc.toUnorderedFiniteSet())
}

/* =========================================================
 *  Edge complements (fixed universe)
 * ========================================================= */

/** Undirected edge-complement on a fixed universe (simple graphs; no loops). */
fun <V : Any> UndirectedGraph<V>.edgeComplementOn(universe: FiniteSet<V>): UndirectedGraph<V> {
    val g = this.embedIntoUniverse(universe)
    val all = AdjacencySetUndirectedGraph.complete(universe).edges
    val comp = (all - g.edges).toUnorderedFiniteSet()
    return AdjacencySetUndirectedGraph.of(universe.toUnorderedFiniteSet(), comp)
}

/** Directed edge-complement on a fixed universe (no loops). */
fun <V : Any> DirectedGraph<V>.edgeComplementOn(universe: FiniteSet<V>): DirectedGraph<V> {
    val g = this.embedIntoUniverse(universe)
    val all = AdjacencySetDirectedGraph.complete(universe).edges
    val comp = (all - g.edges).toUnorderedFiniteSet()
    return AdjacencySetDirectedGraph.of(universe.toUnorderedFiniteSet(), comp)
}