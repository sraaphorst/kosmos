package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.graphs.sparse.CsrAdjacency
import org.vorpal.kosmos.graphs.sparse.CsrAdjacencyBuilder

class CsrDirectedGraph private constructor(
    override val vertices: FiniteSet.Unordered<Int>,
    private val outAdj: CsrAdjacency,
    private val inAdj: CsrAdjacency,
) : DirectedGraph<Int>, FastOutNeighbors<Int>, FastInNeighbors<Int> {

    override val edges: FiniteSet.Unordered<DirectedEdge<Int>> by lazy {
        buildList {
            var u = 0
            while (u < outAdj.vertexCount) {
                val start = outAdj.offsets[u]
                val end = outAdj.offsets[u + 1]
                var i = start
                while (i < end) {
                    add(DirectedEdge(u, outAdj.neighbors[i]))
                    i += 1
                }
                u += 1
            }
        }.toUnorderedFiniteSet()
    }

    private fun nbrs(of: Int, adj: CsrAdjacency): FiniteSet.Unordered<Int> {
        require(of in 0 until adj.vertexCount) { "Vertex $of out of bounds" }

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

    private fun forEachNeighbor(of: Int, adj: CsrAdjacency, action: (Int) -> Unit) {
        require(of in 0 until adj.vertexCount) { "Vertex $of out of bounds" }

        val start = adj.offsets[of]
        val end = adj.offsets[of + 1]

        var i = start
        while (i < end) {
            action(adj.neighbors[i])
            i += 1
        }
    }

    override fun outNeighbors(of: Int): FiniteSet.Unordered<Int> =
        nbrs(of, outAdj)

    /**
     * This is a faster way to iterate over outNeighbors as it avoids creating a FiniteSet: it leverages
     * the Csr property of this representation.
     */
    override fun forEachOutNeighbor(of: Int, action: (Int) -> Unit) {
        forEachNeighbor(of, outAdj, action)
    }

    override fun inNeighbors(of: Int): FiniteSet.Unordered<Int> =
        nbrs(of, inAdj)

    /**
     * This is a faster way to iterate over inNeighbors as it avoids creating a FiniteSet: it leverages
     * the Csr property of this representation.
     */
    override fun forEachInNeighbor(of: Int, action: (Int) -> Unit) {
        forEachNeighbor(of, inAdj, action)
    }

    // ---- The "big" methods: delegate to adjacency-set view ----
    private val asAdj: AdjacencySetDirectedGraph<Int> by lazy {
        AdjacencySetDirectedGraph.of(vertices, edges)
    }

    override fun inducedSubgraph(subvertices: FiniteSet<Int>): DirectedGraph<Int> =
        asAdj.inducedSubgraph(subvertices)

    override fun weaklyConnectedComponentsVertexSets() =
        asAdj.weaklyConnectedComponentsVertexSets()

    override fun weaklyConnectedComponents() =
        asAdj.weaklyConnectedComponents()

    override fun stronglyConnectedComponentsVertexSets() =
        asAdj.stronglyConnectedComponentsVertexSets()

    override fun stronglyConnectedComponents() =
        asAdj.stronglyConnectedComponents()

    override fun toLineGraph() =
        asAdj.toLineGraph()

    override fun toComplementGraph() =
        asAdj.toComplementGraph()

    override fun toTransposeGraph(): DirectedGraph<Int> =
        // Just swap out/in, keep same vertices
        CsrDirectedGraph(vertices, inAdj, outAdj)

    override fun toUndirectedGraph(): UndirectedGraph<Int> =
        asAdj.toUndirectedGraph()

    override infix fun <W : Any> disjointUnion(other: DirectedGraph<W>): DirectedGraph<Either<Int, W>> =
        asAdj.disjointUnion(other)

    override infix fun <W : Any> joinIn(other: DirectedGraph<W>): DirectedGraph<Either<Int, W>> =
        asAdj.joinIn(other)

    override infix fun <W : Any> joinOut(other: DirectedGraph<W>): DirectedGraph<Either<Int, W>> =
        asAdj.joinOut(other)

    override infix fun <W : Any> join(other: DirectedGraph<W>): DirectedGraph<Either<Int, W>> =
        asAdj.join(other)

    override infix fun overlay(other: DirectedGraph<Int>): DirectedGraph<Int> =
        asAdj.overlay(other)

    override infix fun <W : Any> cartesianProduct(other: DirectedGraph<W>): DirectedGraph<Pair<Int, W>> =
        asAdj.cartesianProduct(other)

    override fun <W : Any> mapVertices(f: (Int) -> W): DirectedGraph<W> =
        asAdj.mapVertices(f)

    override fun canonicalizeVertices(): Pair<DirectedGraph<Int>, Map<Int, Int>> =
        this to (0 until outAdj.vertexCount).associateWith { it }

    companion object {
        fun of(vertexCount: Int, arcs: List<Pair<Int, Int>>): CsrDirectedGraph {
            val builderOut = CsrAdjacencyBuilder(vertexCount)
            val builderIn = CsrAdjacencyBuilder(vertexCount)

            arcs.forEach { (u, v) ->
                require(u != v) { "loops not allowed: $u -> $v" }
                builderOut.addArc(u, v)
                builderIn.addArc(v, u)
            }

            val outAdj = builderOut.build()
            val inAdj = builderIn.build()
            val verts = (0 until vertexCount).toUnorderedFiniteSet()
            return CsrDirectedGraph(verts, outAdj, inAdj)
        }
    }
}