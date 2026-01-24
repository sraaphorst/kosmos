package org.vorpal.kosmos.graphs.bipartite

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.graphs.AdjacencySetUndirectedGraph
import org.vorpal.kosmos.graphs.UndirectedEdge
import org.vorpal.kosmos.graphs.UndirectedGraph

/**
 * Primary concrete bipartite graph implementation backed by an explicit edge set.
 *
 * Neighbor queries are supported via cached adjacency maps.
 */
class EdgeListBipartiteGraph<L : Any, R : Any>(
    override val leftVertices: FiniteSet.Unordered<L>,
    override val rightVertices: FiniteSet.Unordered<R>,
    override val edges: FiniteSet.Unordered<Pair<L, R>>,
) : BipartiteGraph<L, R> {

    init {
        edges.forEach { (l, r) ->
            require(l in leftVertices) { "Edge uses left vertex not in left set: $l" }
            require(r in rightVertices) { "Edge uses right vertex not in right set: $r" }
        }
    }

    private val rightAdj: Map<L, FiniteSet.Unordered<R>> by lazy {
        val grouped: Map<L, List<R>> =
            edges.groupBy({ it.first }, { it.second })
        leftVertices.associateWith { l ->
            FiniteSet.unordered(grouped[l] ?: emptyList())
        }
    }

    private val leftAdj: Map<R, FiniteSet.Unordered<L>> by lazy {
        val grouped: Map<R, List<L>> =
            edges.toList().groupBy({ it.second }, { it.first })
        rightVertices.associateWith { r ->
            FiniteSet.unordered(grouped[r] ?: emptyList())
        }
    }

    override fun rightNeighbors(of: L): FiniteSet.Unordered<R> {
        require(of in leftVertices) { "Left vertex not in graph: $of" }
        return rightAdj.getValue(of)
    }

    override fun leftNeighbors(of: R): FiniteSet.Unordered<L> {
        require(of in rightVertices) { "Right vertex not in graph: $of" }
        return leftAdj.getValue(of)
    }

    override fun asUndirectedEitherGraph(): UndirectedGraph<Either<L, R>> {
        val vLeft = leftVertices.map(Either<L, R>::Left).toUnorderedFiniteSet()
        val vRight = rightVertices.map(Either<L, R>::Right).toUnorderedFiniteSet()
        val vertices = (vLeft + vRight).toUnorderedFiniteSet()

        val undirectedEdges = edges.map { (l, r) ->
            UndirectedEdge(Either.left(l), Either.right(r))
        }.toUnorderedFiniteSet()

        return AdjacencySetUndirectedGraph.of(vertices, undirectedEdges)
    }
}
