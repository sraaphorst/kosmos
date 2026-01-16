package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.graphs.sparse.CsrAdjacency

class CsrUndirectedGraph private constructor(
    override val vertices: FiniteSet.Unordered<Int>,
    private val adj: CsrAdjacency,
) : UndirectedGraph<Int>, FastNeighbors<Int>, FastNeighborsStop<Int> {

    override val edges: FiniteSet.Unordered<UndirectedEdge<Int>> by lazy {
        buildList {
            var u = 0
            while (u < adj.vertexCount) {
                val start = adj.offsets[u]
                val end = adj.offsets[u + 1]
                var i = start
                while (i < end) {
                    val v = adj.neighbors[i]
                    if (u < v) add(UndirectedEdge(u, v))
                    i += 1
                }
                u += 1
            }
        }.toUnorderedFiniteSet()
    }

    override fun forEachNeighbor(of: Int, action: (Int) -> Unit) {
        require(of in 0 until adj.vertexCount) { "Vertex $of out of bounds" }

        val start = adj.offsets[of]
        val end = adj.offsets[of + 1]

        var i = start
        while (i < end) {
            action(adj.neighbors[i])
            i += 1
        }
    }

    override fun forEachNeighborUntil(of: Int, f: (Int) -> Boolean) {
        require(of in 0 until adj.vertexCount)
        val start = adj.offsets[of]
        val end = adj.offsets[of + 1]

        var i = start
        while (i < end) {
            if (!f(adj.neighbors[i])) return
            i += 1
        }
    }

    override fun neighbors(of: Int): FiniteSet.Unordered<Int> {
        require(of in vertices) { "Vertex $of not in graph" }

        val start = adj.offsets[of]
        val end = adj.offsets[of + 1]

        val list = buildList(end - start) {
            var i = start
            while (i < end) {
                add(adj.neighbors[i])
                i += 1
            }
        }
        return list.toUnorderedFiniteSet()
    }

    private val asAdj: AdjacencySetUndirectedGraph<Int> by lazy {
        AdjacencySetUndirectedGraph.of(vertices, edges)
    }

    override fun inducedSubgraph(subvertices: FiniteSet<Int>): UndirectedGraph<Int> =
        asAdj.inducedSubgraph(subvertices)

    override fun connectedComponentsVertexSets() =
        asAdj.connectedComponentsVertexSets()

    override fun connectedComponents() =
        asAdj.connectedComponents()

    override fun toLineGraph() =
        asAdj.toLineGraph()

    override fun toComplementGraph() =
        asAdj.toComplementGraph()

    override fun toDirectedGraph(): DirectedGraph<Int> =
        asAdj.toDirectedGraph()

    override infix fun <W : Any> disjointUnion(other: UndirectedGraph<W>): UndirectedGraph<Either<Int, W>> =
        asAdj.disjointUnion(other)

    override infix fun <W : Any> join(other: UndirectedGraph<W>): UndirectedGraph<Either<Int, W>> =
        asAdj.join(other)

    override infix fun overlay(other: UndirectedGraph<Int>): UndirectedGraph<Int> =
        asAdj.overlay(other)

    override infix fun <W : Any> cartesianProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<Int, W>> =
        asAdj.cartesianProduct(other)

    override fun <W : Any> mapVertices(f: (Int) -> W): AdjacencySetUndirectedGraph<W> =
        asAdj.mapVertices(f)

    override fun canonicalizeVertices(): Pair<UndirectedGraph<Int>, Map<Int, Int>> =
        this to (0 until adj.vertexCount).associateWith { it }

    companion object {
        fun of(vertexCount: Int, edges: List<Pair<Int, Int>>): CsrUndirectedGraph {
            val builder = org.vorpal.kosmos.graphs.sparse.CsrAdjacencyBuilder(vertexCount)

            edges.forEach { (u, v) ->
                require(u != v) { "loops not allowed: $u -- $v" }
                builder.addArc(u, v)
                builder.addArc(v, u)
            }

            val adj = builder.build()
            val verts = (0 until vertexCount).toUnorderedFiniteSet()
            return CsrUndirectedGraph(verts, adj)
        }
    }
}