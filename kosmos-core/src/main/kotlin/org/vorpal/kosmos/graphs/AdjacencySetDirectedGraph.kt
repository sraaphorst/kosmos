package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either

class AdjacencySetDirectedGraph<V: Any> private constructor(
    override val vertices: FiniteSet.Unordered<V>,
    private val outAdjacency: Map<V, FiniteSet.Unordered<V>>,
    private val inAdjacency: Map<V, FiniteSet.Unordered<V>>
): DirectedGraph<V> {
    init {
        require(outAdjacency.keys == vertices.backing) {
            "Outgoing adjacency keys must match vertex set."
        }
        require(inAdjacency.keys == vertices.backing) {
            "Incoming adjacency keys must match vertex set."
        }

        // Local membership helper for clarity
        fun checkAllNeighborsWithinVertexSet(
            adjacency: Map<V, FiniteSet.Unordered<V>>,
            direction: String
        ) {
            adjacency.forEach { (vertex, neighbors) ->
                require(neighbors.all { it in vertices }) {
                    "$direction adjacency set for $vertex contains vertex not in vertex set."
                }
            }
        }

        checkAllNeighborsWithinVertexSet(outAdjacency, "Outgoing")
        checkAllNeighborsWithinVertexSet(inAdjacency, "Incoming")

        // Consistency: inAdj and outAdj describe the same edges.
        inAdjacency.forEach { (v, preds) ->
            preds.forEach { u ->
                require(v in outAdjacency.getValue(u)) {
                    "Inconsistency: $u is a predecessor of $v, but $v is not an out-neighbor of $u."
                }
            }
        }

        outAdjacency.forEach { (u, succs) ->
            succs.forEach { v ->
                require(u in inAdjacency.getValue(v)) {
                    "Inconsistency: $v is an out-neighbor of $u, but $u is not a predecessor of $v."
                }
            }
        }
    }

    private fun requireVertex(v: V) {
        require(v in vertices) { "Vertex $v is not in this graph's vertex set." }
    }

    override fun outNeighbors(of: V): FiniteSet.Unordered<V> {
        requireVertex(of)
        return outAdjacency.getValue(of)
    }

    override fun neighbors(of: V): FiniteSet.Unordered<V> =
        outNeighbors(of)

    override fun outEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        outNeighbors(of).map { DirectedEdge(of, it) }.toUnordered()

    override fun outDegree(v: V): Int =
        outNeighbors(v).size

    override fun inNeighbors(of: V): FiniteSet.Unordered<V> {
        requireVertex(of)
        return inAdjacency.getValue(of)
    }

    override fun inEdges(of: V): FiniteSet.Unordered<DirectedEdge<V>> =
        inNeighbors(of).map { DirectedEdge(it, of) }.toUnordered()

    override fun inDegree(v: V): Int =
        inNeighbors(v).size

    override val edges: FiniteSet.Unordered<DirectedEdge<V>> by lazy {
        val allEdges = outAdjacency.flatMap { (from, succs) ->
            succs.map { to -> DirectedEdge(from, to) }
        }
        FiniteSet.unordered(allEdges)
    }

    override fun inducedSubgraph(subvertices: FiniteSet<V>): DirectedGraph<V> {
        subvertices.forEach(::requireVertex)
        val subEdges = edges.filter { edge ->
            edge.from in subvertices && edge.to in subvertices }.toUnordered()
        return of(subvertices.toUnordered(), subEdges)
    }

    private val weakComponentsVertexSetsCache: FiniteSet<FiniteSet<V>> by lazy {
        toUndirectedGraph().connectedComponentsVertexSets()
    }

    private val weakComponentsCache: FiniteSet<DirectedGraph<V>> by lazy {
        weakComponentsVertexSetsCache.map(this::inducedSubgraph)
    }

    private val strongComponentsVertexSetsCache: FiniteSet<FiniteSet.Unordered<V>> by lazy {
        stronglyConnectedComponentSets()
    }

    private val strongComponentsCache: FiniteSet<DirectedGraph<V>> by lazy {
        strongComponentsVertexSetsCache.map(this::inducedSubgraph)
    }

    override fun weaklyConnectedComponentsVertexSets(): FiniteSet<FiniteSet<V>> =
        weakComponentsVertexSetsCache

    override fun weaklyConnectedComponents(): FiniteSet<DirectedGraph<V>> =
        weakComponentsCache

    override fun stronglyConnectedComponentsVertexSets(): FiniteSet<FiniteSet<V>> =
        strongComponentsVertexSetsCache

    override fun stronglyConnectedComponents(): FiniteSet<DirectedGraph<V>> =
        strongComponentsCache

    /**
     * Create the line graph of this directed graph.
     */
    override fun toLineGraph(): AdjacencySetDirectedGraph<DirectedEdge<V>> {
        val vertices = edges
        val lineEdges = edges
            .flatMap { e1 -> edges.filter { e1 canAndThen it }
                .map { e2 -> DirectedEdge(e1, e2) } }
            .toUnorderedFiniteSet()
        return of(vertices, lineEdges)
    }

    /**
     * Create the complement of this directed graph, i.e. for every (u, v) in V x V with u ≠ v, if (u, v)
     * is not in this graph, then (u, v) is in the complement graph.
     */
    override fun toComplementGraph(): AdjacencySetDirectedGraph<V> {
        val complementAdjacencies = vertices
            .associateWith { v -> (vertices - outAdjacency.getValue(v)) - v }
        val edges = complementAdjacencies.entries
            .flatMap { (v, adjList) -> adjList.map { u -> DirectedEdge(v, u) } }
            .toUnorderedFiniteSet()
        return of(vertices, edges)
    }

    /**
     * Transpose this graph, i.e. turn all the edges around.
     */
    override fun toTransposeGraph(): AdjacencySetDirectedGraph<V> =
        of(vertices, edges.map(DirectedEdge<V>::reverse))

    /**
     * Turn this directed graph into an undirected graph by replacing each edge with an undirected edge.
     */
    override fun toUndirectedGraph(): AdjacencySetUndirectedGraph<V> =
        AdjacencySetUndirectedGraph.of(vertices, edges.map(DirectedEdge<V>::toUndirectedEdge))

    override infix fun <W: Any> disjointUnion(other: DirectedGraph<W>): AdjacencySetDirectedGraph<Either<V, W>> {
        val vSet = (vertices.map { v -> Either.leftAs<V, W>(v) } +
                other.vertices.map { v -> Either.rightAs<V, W>(v) })
            .toUnorderedFiniteSet()

        val thisEdges = edges.map { (u, v) ->
            DirectedEdge(Either.leftAs<V, W>(u), Either.leftAs<V, W>(v)) }
        val otherEdges = other.edges.map { (u, v) ->
            DirectedEdge(Either.rightAs<V, W>(u), Either.rightAs<V, W>(v)) }
        val allEdges = (thisEdges + otherEdges).toUnorderedFiniteSet()
        return of(vSet, allEdges)
    }

    override infix fun <W: Any> joinIn(other: DirectedGraph<W>): AdjacencySetDirectedGraph<Either<V, W>> =
        joinImpl(other, leftToRight = true, rightToLeft = false)

    override infix fun <W: Any> joinOut(other: DirectedGraph<W>): AdjacencySetDirectedGraph<Either<V, W>> =
        joinImpl(other, leftToRight = false, rightToLeft = true)

    override infix fun <W: Any> join(other: DirectedGraph<W>): AdjacencySetDirectedGraph<Either<V, W>> =
        joinImpl(other, leftToRight = true, rightToLeft = true)

    private fun <W: Any> joinImpl(other: DirectedGraph<W>,
                                  leftToRight: Boolean,
                                  rightToLeft: Boolean): AdjacencySetDirectedGraph<Either<V, W>> {
        val duGraph = this disjointUnion other

        val inEdgeList = if (leftToRight)
            vertices.flatMap { v ->
                other.vertices.map { u ->
                    DirectedEdge(Either.left(v), Either.right(u))
                }
            }
        else emptyList()

        val outEdgeList = if (rightToLeft)
            vertices.flatMap { v ->
                other.vertices.map { u ->
                    DirectedEdge(Either.right(u), Either.left(v))
                }
            }
        else emptyList()

        val allEdges = (inEdgeList + outEdgeList + duGraph.edges).toUnorderedFiniteSet()
        return of(duGraph.vertices, allEdges)

    }

    override infix fun overlay(other: DirectedGraph<V>): AdjacencySetDirectedGraph<V> {
        val vSet = (vertices + other.vertices).toUnorderedFiniteSet()
        val allEdges = (edges + other.edges).toUnorderedFiniteSet()
        return of(vSet, allEdges)
    }

    override infix fun <W: Any> cartesianProduct(other: DirectedGraph<W>): AdjacencySetDirectedGraph<Pair<V, W>> {
        val vSet = vertices.cartesianProduct(other.vertices).toUnordered()

        // Add edges from this graph, i.e. for each edge (u1, u2) in this graph, ((u1, v), (u2, v)) is
        // an edge of the new graph for all v in other.
        val eSet1 = edges.flatMap { (u1, u2) ->
            other.vertices.map { v -> DirectedEdge(u1 to v, u2 to v) } }

        // Add edges from the other graph, i.e. for each edge (v1, v2) in other, ((u, v1), (u, v2)) is
        // an edge of the new graph for all v in this.
        val eSet2 = other.edges.flatMap { (v1, v2) ->
            vertices.map { u -> DirectedEdge(u to v1, u to v2)}}

        return of(vSet, (eSet1 + eSet2).toUnorderedFiniteSet())
    }

    override fun <W : Any> mapVertices(f: (V) -> W): AdjacencySetDirectedGraph<W> {
        val newVertices = vertices.map(f).toUnorderedFiniteSet()
        val newEdges = edges
            .map { e -> DirectedEdge(f(e.from), f(e.to)) }
            .toUnorderedFiniteSet()
        return of(newVertices, newEdges)
    }

    override fun canonicalizeVertices(): Pair<AdjacencySetDirectedGraph<Int>, Map<Int, V>> {
        val ordered = vertices.toOrdered()
        val vToIndex = ordered.order.withIndex().associate { (i, v) -> v to i }
        val g2 = mapVertices { v: V -> vToIndex.getValue(v) }
        val indexToV = vToIndex.entries.associate { (v, i) -> i to v }
        return g2 to indexToV
    }

    companion object {
        fun <V: Any> of(
            vertices: FiniteSet<V>,
            edges: FiniteSet<DirectedEdge<V>>
        ): AdjacencySetDirectedGraph<V> {
            val vSet = vertices.toUnordered()

            val outInPairs =
                edges.flatMap { e ->
                    val (from, to) = e
                    require(from in vSet) { "Arc $e originates from vertex $from, which is not in this graph's vertex set." }
                    require(to in vSet) { "Arc $e ends in vertex $to, which is not in this graph's vertex set." }
                    listOf(from to to)
                }
            val outInIncidenceMap = outInPairs.groupBy({ it.first }, { it.second })
            val outAdjacency = vSet.associateWith { v -> FiniteSet.unordered(outInIncidenceMap[v] ?: emptyList()) }

            val inOutPairs = outInPairs.map { (a, b) -> b to a}
            val inOutIncidenceMap = inOutPairs.groupBy({ it.first }, { it.second })
            val inAdjacency = vSet.associateWith { v ->
                FiniteSet.unordered(inOutIncidenceMap[v] ?: emptyList()) }

            return AdjacencySetDirectedGraph(vSet, outAdjacency, inAdjacency)
        }

        fun <V: Any> empty() =
            of(FiniteSet.empty(), FiniteSet.empty<DirectedEdge<V>>())

        fun <V: Any> edgeless(vertices: FiniteSet<V>): AdjacencySetDirectedGraph<V> =
            of(vertices.toUnordered(), FiniteSet.empty())

        fun <V: Any> complete(vertices: FiniteSet<V>): AdjacencySetDirectedGraph<V> {
            val edges = vertices.flatMap { v ->
                vertices.mapNotNull { u -> if (v == u) null else DirectedEdge(u, v) } }
            return of(vertices.toUnordered(), edges.toUnorderedFiniteSet())
        }

        fun complete(n: Int): AdjacencySetDirectedGraph<Int> =
            complete((0 until n).toUnorderedFiniteSet())

        fun <V: Any> k1(v: V): AdjacencySetDirectedGraph<V> =
            complete(FiniteSet.unordered(v))

        val k1: AdjacencySetDirectedGraph<Int> = k1(0)
    }
}
