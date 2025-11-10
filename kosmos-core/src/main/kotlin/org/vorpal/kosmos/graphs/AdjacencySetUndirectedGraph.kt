package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet

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

    override fun neighbors(of: V): FiniteSet.Unordered<V> {
        require(of in vertices) { "Vertex $of is not in this graph's vertex set."}
        return adjacency.getValue(of)
    }

    override val edges: FiniteSet.Unordered<UndirectedEdge<V>> by lazy {
        val allEdges = adjacency.flatMap { (u, neighborhood) ->
            neighborhood.map { v -> UndirectedEdge(u, v) }
        }
        FiniteSet.unordered(allEdges)
    }

    companion object {
        fun <V: Any> of(
            vertices: FiniteSet<V>,
            edges: FiniteSet<UndirectedEdge<V>>
        ): AdjacencySetUndirectedGraph<V> {

            // Build list of (vertex, neighbor) pairs.
            val incidentPairs: List<Pair<V, V>> =
                edges.flatMap { e ->
                    val (u, v) = e
                    require(u in vertices) { "Edge $e uses vertex $u, which is not in this graph's vertex set." }
                    require(v in vertices) { "Edge $e uses vertex $v, which is not in this graph's vertex set." }
                    listOf(u to v, v to u)
                }

            // Group neighbors by vertex: Map<V, List<V>>.
            val incidentMap: Map<V, List<V>> = incidentPairs.groupBy({ it.first }, { it.second })
            val adjacencies: Map<V, FiniteSet.Unordered<V>> = vertices
                .associateWith { v -> FiniteSet.unordered(incidentMap[v] ?: emptyList()) }

            return AdjacencySetUndirectedGraph(vertices.toUnordered(), adjacencies)
        }
    }
}
