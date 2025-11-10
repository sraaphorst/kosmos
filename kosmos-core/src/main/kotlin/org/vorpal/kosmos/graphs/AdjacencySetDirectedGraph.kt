package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet

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

    companion object {
        fun <V: Any> of(
            vertices: FiniteSet<V>,
            edges: FiniteSet<DirectedEdge<V>>
        ): AdjacencySetDirectedGraph<V> {
            val vSet = vertices.toUnordered()

            val outInPairs: List<Pair<V, V>> =
                edges.flatMap { e ->
                    val (from, to) = e
                    require(from in vSet) { "Arc $e originates from vertex $from, which is not in this graph's vertex set." }
                    require(to in vSet) { "Arc $e ends in vertex $to, which is not in this graph's vertex set." }
                    listOf(from to to)
                }
            val outInIncidenceMap = outInPairs.groupBy({ it.first }, { it.second })
            val outAdjacency = vSet.associateWith { v -> FiniteSet.unordered(outInIncidenceMap[v] ?: emptyList()) }

            val inOutPairs: List<Pair<V, V>> = outInPairs.map { (a, b) -> b to a}
            val inOutIncidenceMap = inOutPairs.groupBy({ it.first }, { it.second })
            val inAdjacency = vSet.associateWith { v -> FiniteSet.unordered(inOutIncidenceMap[v] ?: emptyList()) }

            return AdjacencySetDirectedGraph(vSet, outAdjacency, inAdjacency)
        }
    }
}