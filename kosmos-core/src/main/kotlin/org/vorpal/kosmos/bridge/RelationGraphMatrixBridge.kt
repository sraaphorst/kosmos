package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras.F2
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toOrderedFiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.DirectedEdge
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.linear.Matrix

/**
 * A functorial bridge between finite binary relations, directed graphs,
 * and square matrices over the field F₂, which are all isomorphic provided an ordering is
 * established on the elements of the relation / the vertices of the graph.
 */
object RelationGraphMatrixBridge {

    /** Relation → DirectedGraph */
    fun <V: Any> Relation<V>.toRelationalGraph(elements: FiniteSet<V>): DirectedGraph<V> {
        val vertices = elements.toUnordered()
        val edges = vertices.flatMap { u ->
            vertices.filter { v -> rel(u, v) }
                .map { v -> DirectedEdge(u, v) }
        }.toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(vertices, edges)
    }

    /** DirectedGraph → Relation */
    fun <V: Any> DirectedGraph<V>.toRelation(): Relation<V> =
        Relation { u, v -> v in this.neighbors(u) }

    /** DirectedGraph → Matrix(F₂) */
    fun <V: Any> DirectedGraph<V>.toMatrix(): Matrix<Int> {
        val ordered = vertices.toOrderedFiniteSet()
        val n = ordered.size
        val data = List(n) { i ->
            List(n) { j ->
                if (DirectedEdge(ordered[i], ordered[j]) in edges) 1 else 0
            }
        }
        return Matrix(n, n, F2, data)
    }

    /** Matrix(F₂) → DirectedGraph<Int> */
    fun Matrix<Int>.toRelationalGraph(): DirectedGraph<Int> {
        require(rows == cols) { "Matrix must be square, but has dimensions $rows × $cols" }
        val n = rows
        val vertices = (0 until n).toUnorderedFiniteSet()
        val edges = buildList {
            for (i in 0 until n)
                for (j in 0 until n)
                    if (get(i, j) == 1) add(DirectedEdge(i, j))
        }.toUnorderedFiniteSet()

        return AdjacencySetDirectedGraph.of(vertices, edges)
    }

    /** Matrix(F₂) → Relation<Int> */
    fun Matrix<Int>.toRelation(): Relation<Int> {
        require(n == m) { "Matrix must be square, but has dimensions $n × $m" }
        return Relation { i, j -> get(i, j) == 1 }
    }

    /** Relation<Int> → Matrix(F₂) */
    fun <V> Relation<V>.toMatrix(elements: FiniteSet<V>): Matrix<Int> {
        val list = elements.toOrderedFiniteSet()
        val n = list.size
        val data = List(n) { i ->
            List(n) { j ->
                if (rel(list[i], list[j])) 1 else 0
            }
        }
        return Matrix(n, n, F2, data)
    }
}
