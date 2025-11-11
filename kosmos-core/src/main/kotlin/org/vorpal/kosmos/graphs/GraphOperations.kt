package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either

/**
 * Vertex types for the corona product of two graphs.
 * A vertex is either from the graph on the left,
 * or from its copy of the graph on the right.
 */
typealias CoronaVertex<V, W> = Either<V, Pair<V, W>>

/**
 * Constructs the corona product `G ⊙ H` of this undirected graph `G` with another
 * undirected graph `H`.
 *
 * Vertices:
 *  - one "center" vertex for each vertex `v ∈ V(G)`, and
 *  - for each `v ∈ V(G)`, one "satellite" vertex `(v, w)` for every `w ∈ V(H)`.
 *
 * Formally:
 * ```kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *```
 *
 * Edges:
 *  1. All edges of `G` between center vertices.
 *  2. For each `v ∈ V(G)`, a copy of `H` on the satellites `{ {v, w} : w ∈ V(H) }`.
 *  3. For each `v ∈ V(G)` and `w ∈ V(H)`, an edge between the center `v` and its satellite `{v, w}`.
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
        false,
        true,
        true
    )

/**
 * Constructs the corona product `G ⊙ H` of this directed graph `G` with another
 * directed graph `H`.
 *
 * Vertices:
 *  - one "center" vertex for each vertex `v ∈ V(G)`, and
 *  - for each `v ∈ V(G)`, one "satellite" vertex `(v, w)` for every `w ∈ V(H)`.
 *
 * Formally:
 * ```kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *```
 *
 * Edges:
 *  1. All edges of `G` between center vertices.
 *  2. For each `v ∈ V(G)`, a copy of `H` on the satellites `{ (v, w) : w ∈ V(H) }`.
 *  3. For each `v ∈ V(G)` and `w ∈ V(H)`, **an edge to the center from its satellite `(w, v)`.**
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
        true,
        true,
        false
    )

/**
 * Constructs the corona product `G ⊙ H` of this directed graph `G` with another
 * directed graph `H`.
 *
 * Vertices:
 *  - one "center" vertex for each vertex `v ∈ V(G)`, and
 *  - for each `v ∈ V(G)`, one "satellite" vertex `(v, w)` for every `w ∈ V(H)`.
 *
 * Formally:
 * ```kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *```
 *
 * Edges:
 *  1. All edges of `G` between center vertices.
 *  2. For each `v ∈ V(G)`, a copy of `H` on the satellites `{ (v, w) : w ∈ V(H) }`.
 *  3. For each `v ∈ V(G)` and `w ∈ V(H)`, **an edge from the center to its satellite `(v, w)`.**
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
        true,
        false,
        true
    )

/**
 * Constructs the corona product `G ⊙ H` of this directed graph `G` with another
 * directed graph `H`.
 *
 * Vertices:
 *  - one "center" vertex for each vertex `v ∈ V(G)`, and
 *  - for each `v ∈ V(G)`, one "satellite" vertex `(v, w)` for every `w ∈ V(H)`.
 *
 * Formally:
 * ```kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *```
 *
 * Edges:
 *  1. All edges of `G` between center vertices.
 *  2. For each `v ∈ V(G)`, a copy of `H` on the satellites `{ (v, w) : w ∈ V(H) }`.
 *  3. For each `v ∈ V(G)` and `w ∈ V(H)`, **edges between the center and its satellite `(v, w)`, `(w, v)`.**
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
        true,
        true,
        true
    )



/**
 * Constructs the corona product `G ⊙ H` of this undirected graph `G` with another
 * undirected graph `H`.
 *
 * Vertices:
 *  - one "center" vertex for each vertex `v ∈ V(G)`, and
 *  - for each `v ∈ V(G)`, one "satellite" vertex `(v, w)` for every `w ∈ V(H)`.
 *
 * Formally:
 * ```kotlin
 *  V(G ⊙ H) = V(G) ∪ { (v, w) : v ∈ V(G), w ∈ V(H) }.
 *```
 *
 * Edges:
 *  1. All edges of `G` between center vertices.
 *  2. For each `v ∈ V(G)`, a copy of `H` on the satellites `{ (v, w) : w ∈ V(H) }`.
 *  3. For each `v ∈ V(G)` and `w ∈ V(H)`, an edge between the center `v` and its satellite `(v, w)`.
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
