package org.vorpal.kosmos.graphs.bipartite

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.graphs.UndirectedGraph

/**
 * A finite bipartite graph with a distinguished left vertex set L and right vertex set R.
 *
 * Edges are undirected but always run between the parts:
 *   edges ⊆ L × R
 *
 * This structure is first-class (not encoded as Either<L,R>),
 * but provides a bridge to an UndirectedGraph<Either<L,R>> when you want generic graph algorithms.
 */
interface BipartiteGraph<L : Any, R : Any> {
    val leftVertices: FiniteSet.Unordered<L>
    val rightVertices: FiniteSet.Unordered<R>

    val isEmpty: Boolean
        get() = leftVertices.isEmpty && rightVertices.isEmpty
    val isNotEmpty: Boolean
        get() = !isEmpty

    val isComplete: Boolean
        get() = edgeCount == leftOrder * rightOrder
    val isNotComplete: Boolean
        get() = !isComplete

    /** Edge set as pairs (l, r). */
    val edges: FiniteSet.Unordered<Pair<L, R>>

    val leftOrder: Int
        get() = leftVertices.size

    val rightOrder: Int
        get() = rightVertices.size

    val edgeCount: Int
        get() = edges.size

    /** Right-neighbors of a left vertex. */
    fun rightNeighbors(of: L): FiniteSet.Unordered<R>

    /** Left-neighbors of a right vertex. */
    fun leftNeighbors(of: R): FiniteSet.Unordered<L>

    fun leftDegree(of: L): Int =
        rightNeighbors(of).size

    fun rightDegree(of: R): Int =
        leftNeighbors(of).size

    fun hasEdge(l: L, r: R): Boolean =
        (l to r) in edges

    /**
     * Bridge: view this as an undirected graph on the disjoint union L ⊔ R,
     * represented as Either<L,R>.
     */
    fun asUndirectedEitherGraph(): UndirectedGraph<Either<L, R>>

    companion object {
        fun <L : Any, R : Any> of(
            leftVertices: FiniteSet.Unordered<L>,
            rightVertices: FiniteSet.Unordered<R>,
            edges: FiniteSet.Unordered<Pair<L, R>>,
        ): BipartiteGraph<L, R> =
            EdgeListBipartiteGraph(leftVertices, rightVertices, edges)

        fun <L: Any, R : Any> empty(): BipartiteGraph<L, R> =
            of(FiniteSet.emptyUnordered(), FiniteSet.emptyUnordered(), FiniteSet.emptyUnordered())

        fun <L : Any, R : Any> complete(
            leftVertices: FiniteSet.Unordered<L>,
            rightVertices: FiniteSet.Unordered<R>
        ): BipartiteGraph<L, R> {
            val edges =
                leftVertices
                    .cartesianProduct(rightVertices)
                    .map { (l, r) -> l to r }
                    .toUnorderedFiniteSet()

            return of(leftVertices, rightVertices, edges)
        }

        // Convert a set of vertices and a list of edges to an incidence bipartite graph.
        fun <V : Any> incidence(
            vertices: FiniteSet.Unordered<V>,
            edges: List<FiniteSet.Unordered<V>>
        ): BipartiteGraph<V, Int> {
            require(edges.all { e -> e.all { it in vertices } }) {
                "Incidence edge contains vertex not in vertex set."
            }
            val edgeIndices = edges.indices.toUnorderedFiniteSet()
            val incidencePairs = edges.flatMapIndexed { index, edgeSet ->
                edgeSet.map { v -> v to index }
            }.toUnorderedFiniteSet()
            return of(vertices, edgeIndices, incidencePairs)
        }
    }
}
