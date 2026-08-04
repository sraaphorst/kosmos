package org.vorpal.kosmos.graphs

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.finiteset.minus
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.graphs.bipartite.BipartiteGraph

/**
 * Regression tests for the graph counting layer (issue #221):
 *
 *  - [Graph.vertexCount] / [Graph.edgeCount] agree with the sizes of the
 *    materialized [Graph.vertices] / edges sets on every concrete graph type.
 *  - [UndirectedGraph.isComplete] and [BipartiteGraph.isComplete] are correct
 *    on both sides of the old `Int` overflow boundary, where the previous
 *    `Int`-arithmetic formulas produced false positives.
 */
class GraphCountingSpec : StringSpec({

    val arbVertex = Arb.int(0..999)

    // ===== Counts agree with materialized sizes, per concrete type =====

    "AdjacencySetUndirectedGraph: vertexCount and edgeCount agree with .size" {
        checkAll(ArbGraph.undirectedGnP(arbVertex, 0..30, 0.3)) { g ->
            g.vertexCount shouldBe g.vertices.size.toLong()
            g.edgeCount shouldBe g.edges.size.toLong()
        }
    }

    "AdjacencySetDirectedGraph: vertexCount and edgeCount agree with .size" {
        checkAll(ArbGraph.directedGnP(arbVertex, 0..30, 0.3)) { g ->
            g.vertexCount shouldBe g.vertices.size.toLong()
            g.edgeCount shouldBe g.edges.size.toLong()
        }
    }

    "CsrUndirectedGraph: vertexCount and edgeCount agree with .size" {
        checkAll(ArbGraph.undirectedGnP(Arb.int(0..99), 0..30, 0.3)) { base ->
            // Relabel to the dense Int range the CSR representation requires.
            val (canon, _) = base.canonicalizeVertices()
            val csr = CsrUndirectedGraph.of(
                canon.vertices.size,
                canon.edges.map { it.u to it.v }.toList()
            )
            csr.vertexCount shouldBe csr.vertices.size.toLong()
            csr.edgeCount shouldBe csr.edges.size.toLong()
            csr.edgeCount shouldBe canon.edgeCount
        }
    }

    "CsrDirectedGraph: vertexCount and edgeCount agree with .size" {
        checkAll(ArbGraph.directedGnP(Arb.int(0..99), 0..30, 0.3)) { base ->
            val (canon, _) = base.canonicalizeVertices()
            val csr = CsrDirectedGraph.of(
                canon.vertices.size,
                canon.edges.map { it.from to it.to }.toList()
            )
            csr.vertexCount shouldBe csr.vertices.size.toLong()
            csr.edgeCount shouldBe csr.edges.size.toLong()
            csr.edgeCount shouldBe canon.edgeCount
        }
    }

    "BipartiteGraph: vertex and edge counts agree with .size" {
        val left = (0 until 40).toUnorderedFiniteSet()
        val right = (100 until 130).toUnorderedFiniteSet()
        val g = BipartiteGraph.complete(left, right)
        g.leftVertexCount shouldBe left.size.toLong()
        g.rightVertexCount shouldBe right.size.toLong()
        g.edgeCount shouldBe g.edges.size.toLong()
        g.edgeCount shouldBe 40L * 30L
    }

    // ===== isComplete: ordinary (small) cases =====

    "isComplete is true for small complete graphs, including K_0 and K_1" {
        for (n in 0..8) {
            AdjacencySetUndirectedGraph.complete(n).isComplete() shouldBe true
        }
    }

    "isComplete is false for K_n minus one edge, and for edgeless graphs with n >= 2" {
        for (n in 2..8) {
            val kn = AdjacencySetUndirectedGraph.complete(n)
            val missing = kn.edges.first()
            val notQuite = AdjacencySetUndirectedGraph.of(
                kn.vertices,
                (kn.edges - missing).toUnordered()
            )
            notQuite.isComplete() shouldBe false

            AdjacencySetUndirectedGraph.edgeless((0 until n).toUnorderedFiniteSet())
                .isComplete() shouldBe false
        }
    }

    // ===== isComplete: the Int overflow regression =====

    "isComplete regression: n = 92,683 with 55,607 edges is not complete (old Int formula said it was)" {
        // The old implementation compared edges.size against order * (order - 1) / 2
        // computed in Int arithmetic. For n = 92,683 that product wraps around to
        // exactly 55,607 — so a wildly incomplete graph passed as complete.
        val n = 92_683

        // Document the overflow itself: this IS the old formula's Int value.
        @Suppress("INTEGER_OVERFLOW")
        val oldIntFormula = n * (n - 1) / 2
        oldIntFormula shouldBe 55_607

        // The true count does not fit in an Int.
        val trueCount = n.toLong() * (n - 1) / 2
        trueCount shouldBe 4_295_022_903L

        // Build a graph on n vertices with exactly 55,607 edges (a star from vertex 0).
        val g = CsrUndirectedGraph.of(n, (1..55_607).map { 0 to it })
        g.vertexCount shouldBe n.toLong()
        g.edgeCount shouldBe 55_607L

        // Old formula: 55,607 == 55,607 → true (the bug). New Long formula: false.
        g.isComplete() shouldBe false
    }

    "BipartiteGraph isComplete regression: empty graph on 65,536 x 65,536 parts is not complete" {
        // The old implementation compared edgeCount against leftOrder * rightOrder in
        // Int arithmetic. 65,536 * 65,536 = 2^32 wraps to exactly 0 — so the EMPTY
        // bipartite graph on those parts passed as complete.
        val m = 65_536

        @Suppress("INTEGER_OVERFLOW")
        val oldIntFormula = m * m
        oldIntFormula shouldBe 0

        val left = (0 until m).toUnorderedFiniteSet()
        val right = (m until 2 * m).toUnorderedFiniteSet()
        val g = BipartiteGraph.of(left, right, emptyList<Pair<Int, Int>>().toUnorderedFiniteSet())

        g.edgeCount shouldBe 0L
        g.leftVertexCount * g.rightVertexCount shouldBe 4_294_967_296L

        // Old formula: 0 == 0 → true (the bug). New Long formula: false.
        g.isComplete shouldBe false
    }

    // ===== Predicates swept in #221 still behave =====

    "hasCycle / isTree / isForest agree with their counting identities on random graphs" {
        checkAll(ArbGraph.undirectedGnP(arbVertex, 0..25, 0.15)) { g ->
            val c = g.connectedComponents().size
            g.isForest() shouldBe (g.edgeCount == g.vertexCount - c)
            g.hasCycle() shouldBe (g.edgeCount > g.vertexCount - c)
            g.isTree() shouldBe (c == 1 && g.edgeCount == g.vertexCount - 1)
            // A forest never has a cycle; a tree is a connected forest.
            if (g.isTree()) g.isForest() shouldBe true
            if (g.isForest()) g.hasCycle() shouldBe false
        }
    }
})
