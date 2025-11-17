package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either

class AdjacencySetUndirectedGraph<V: Any> private constructor(
    override val vertices: FiniteSet.Unordered<V>,
    private val adjacency: Map<V, FiniteSet.Unordered<V>>
): UndirectedGraph<V> {
    init {
        // Sanity checks: keys match vertex set, and neighbors are within vertex set.
        require(adjacency.keys == vertices.backing) {
            "Adjacency keys must match vertex set."
        }
        adjacency.forEach { (vertex, neighborhood ) ->
            require(neighborhood.all { it in vertices }) {
                "Adjacency list for $vertex contains vertex not in vertex set."
            }
        }
    }

    private fun requireVertex(v: V) {
        require(v in vertices) { "Vertex $v is not in this graph's vertex set." }
    }

    override fun neighbors(of: V): FiniteSet.Unordered<V> {
        requireVertex(of)
        return adjacency.getValue(of)
    }

    override val edges: FiniteSet.Unordered<UndirectedEdge<V>> by lazy {
        val allEdges = adjacency.flatMap { (u, neighborhood) ->
            neighborhood.map { v -> UndirectedEdge(u, v) }
        }
        FiniteSet.unordered(allEdges)
    }

    override fun inducedSubgraph(subvertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> {
        subvertices.forEach(::requireVertex)
        val subEdges = edges.filter { edge -> edge.u in subvertices && edge.v in subvertices }.toUnordered()
        return of(subvertices.toUnordered(), subEdges)
    }

    private val componentsVertexSetsCache: FiniteSet<FiniteSet<V>> by lazy {
        computeConnectedComponentVertexSets()
    }

    private val componentsCache: FiniteSet<UndirectedGraph<V>> by lazy {
        componentsVertexSetsCache.map(::inducedSubgraph)
    }

    override fun connectedComponentsVertexSets(): FiniteSet<FiniteSet<V>> =
        componentsVertexSetsCache

    /**
     * Decompose this undirected graph into its (weakly) connected components.
     *
     * Two vertices u and v lie in the same component iff there is an undirected
     * path between them.
     * The result is a finite set of induced subgraphs, one for
     * each equivalence class of vertices under this relation.
     *
     * Implementation:
     *  - Repeatedly pick an unvisited vertex, run [bfs] from it, and form the
     *    induced subgraph on the discovered vertex set.
     *  - The results are calculated once and cached, so repeated calls to
     *    connectedComponents do not require any additional computation.
     *
     * Complexity:
     *  - Time: O(|V| + |E|) overall.
     *  - Space: O(|V|) for bookkeeping plus the resulting component graphs.
     */
    override fun connectedComponents(): FiniteSet<UndirectedGraph<V>> =
        componentsCache

    /**
     * Create the line graph of an undirected graph.
     */
    override fun toLineGraph(): AdjacencySetUndirectedGraph<UndirectedEdge<V>> {
        val lineGraphEdges = edges
            .flatMap { e1 -> edges.filter { e2 -> e1 incidentTo e2  }
                .map { e2 -> UndirectedEdge(e1, e2) } }
            .toUnorderedFiniteSet()
        return of(edges, lineGraphEdges)
    }

    /**
     * Take the complement graph, i.e. the graph that contains edge e in VxV if:
     * 1. e is not a loop; and
     * 2. e is not in this graph.
     */
    override fun toComplementGraph(): AdjacencySetUndirectedGraph<V> {
        val complementAdjacencies = vertices.associateWith { v ->
            (vertices - adjacency.getValue(v)) - v }
        val edges = complementAdjacencies.entries.flatMap { (v, adjList) ->
            adjList.map { u -> UndirectedEdge(v, u) } }
            .toUnorderedFiniteSet()
        return of(vertices, edges)
    }

    /**
     * Create a directed representation of this graph where each undirected edge is replaced by two perpendicular
     * directed edges.
     */
    override fun toDirectedGraph(): DirectedGraph<V> =
        AdjacencySetDirectedGraph.of(
            vertices.toUnorderedFiniteSet(),
            edges.flatMap { it.toDirectedEdges().toList() }.toUnorderedFiniteSet()
        )

    override infix fun <W: Any> cartesianProduct(other: UndirectedGraph<W>): AdjacencySetUndirectedGraph<Pair<V, W>> {
        val vSet = vertices.cartesianProduct(other.vertices).toUnorderedFiniteSet()

        // Add edges from this graph, i.e. for each edge {u1, u2} in this graph, {(u1, v), (u2, v)} is
        // an edge of the new graph for all v in other.
        val eSet1 = edges.flatMap { (u1, u2) ->
            other.vertices.map { v -> UndirectedEdge(u1 to v, u2 to v)}}

        // Add edges from the other graph, i.e. for each edge {v1, v2} in other, {(u, v1), (u, v2)} is
        // an edge of the new graph for all v in this.
        val eSet2 = other.edges.flatMap { (v1, v2) ->
            vertices.map { u -> UndirectedEdge(u to v1, u to v2)}
        }

        return of(vSet, (eSet1 + eSet2).toUnorderedFiniteSet())
    }

    override infix fun <W: Any> disjointUnion(other: UndirectedGraph<W>): AdjacencySetUndirectedGraph<Either<V, W>> {
        val vSet = (vertices.map { v -> Either.leftAs<V, W>(v) } +
                other.vertices.map { v -> Either.rightAs<V, W>(v) })
            .toUnorderedFiniteSet()

        val thisEdges = edges.map { (u, v) ->
            UndirectedEdge(Either.leftAs<V, W>(u), Either.leftAs<V, W>(v)) }
        val otherEdges = other.edges.map { (u, v) ->
            UndirectedEdge(Either.rightAs<V, W>(u), Either.rightAs<V, W>(v)) }
        val allEdges = (thisEdges + otherEdges).toUnorderedFiniteSet()
        return of(vSet, allEdges)
    }

    override infix fun <W: Any> join(other: UndirectedGraph<W>): AdjacencySetUndirectedGraph<Either<V, W>> {
        val duGraph = this disjointUnion other

        // Add all the edges between the two graphs.
        val newEdges = this.vertices.flatMap { v ->
            other.vertices.map { u ->
                UndirectedEdge(Either.left(v), Either.right(u)) } }
            .toUnorderedFiniteSet()

        return of(duGraph.vertices, (duGraph.edges + newEdges).toUnorderedFiniteSet())
    }

    override infix fun overlay(other: UndirectedGraph<V>): AdjacencySetUndirectedGraph<V> {
        val vSet = (vertices + other.vertices).toUnorderedFiniteSet()
        val allEdges = (edges + other.edges).toUnorderedFiniteSet()
        return of(vSet, allEdges)
    }

    override fun <W : Any> mapVertices(
        f: (V) -> W
    ): AdjacencySetUndirectedGraph<W> {
        val newVertices = vertices.map(f).toUnorderedFiniteSet()
        val newEdges = edges
            .map { e -> UndirectedEdge(f(e.u), f(e.v)) }
            .toUnorderedFiniteSet()
        return of(newVertices, newEdges)
    }

    override fun canonicalizeVertices(): Pair<AdjacencySetUndirectedGraph<Int>, Map<Int, V>> {
        val ordered = vertices.toOrdered()
        val vToIndex = ordered.order.withIndex().associate { (i, v) -> v to i }
        val g2 = mapVertices { v: V -> vToIndex.getValue(v) }
        val indexToV = vToIndex.entries.associate { (v, i) -> i to v }
        return g2 to indexToV
    }

    companion object {
        fun <V: Any> of(
            vertices: FiniteSet<V>,
            edges: FiniteSet<UndirectedEdge<V>>
        ): AdjacencySetUndirectedGraph<V> {

            // Build list of (vertex, neighbor) pairs.
            val incidentPairs =
                edges.flatMap { e ->
                    val (u, v) = e
                    require(u in vertices) { "Edge $e uses vertex $u, which is not in this graph's vertex set." }
                    require(v in vertices) { "Edge $e uses vertex $v, which is not in this graph's vertex set." }
                    listOf(u to v, v to u)
                }

            // Group neighbors by vertex: Map<V, List<V>>.
            val incidentMap = incidentPairs.groupBy({ it.first }, { it.second })
            val adjacencies = vertices
                .associateWith { v -> FiniteSet.unordered(incidentMap[v] ?: emptyList()) }

            return AdjacencySetUndirectedGraph(vertices.toUnordered(), adjacencies)
        }

        fun <V: Any> empty() = of(FiniteSet.empty(), FiniteSet.empty<UndirectedEdge<V>>())

        fun <V: Any> edgeless(vertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> =
            of(vertices.toUnordered(), FiniteSet.empty())

        fun <V: Any> complete(vertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> {
            val edges = vertices.flatMap { v ->
                vertices.mapNotNull { u -> if (v == u) null else UndirectedEdge(u, v) } }
            return of(vertices.toUnordered(), edges.toUnorderedFiniteSet())
        }

        fun complete(n: Int): AdjacencySetUndirectedGraph<Int> =
            complete((0 until n).toUnorderedFiniteSet())

        fun <V: Any> k1(v: V): AdjacencySetUndirectedGraph<V> =
            complete(FiniteSet.unordered(v))
    }
}
