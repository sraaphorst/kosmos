package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.instances.F2
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toOrderedFiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.graphs.core.Edge
import org.vorpal.kosmos.graphs.core.RelationalGraph
import org.vorpal.kosmos.linear.Matrix
import org.vorpal.kosmos.relations.Relation

/**
 * A functorial bridge between finite binary relations, relational graphs,
 * and square matrices over the field F₂, which are all isomorphic provided an ordering is
 * established on the elements of the relation / the vertices of the graph.
 */
object RelationGraphMatrixBridge {

    /** Relation → RelationalGraph */
    fun <V> Relation<V>.toRelationalGraph(elements: FiniteSet<V>): RelationalGraph<V> =
        object : RelationalGraph<V> {
            override val vertices = elements.toOrderedFiniteSet()
            override val edges = elements.flatMap { u ->
                elements.mapNotNull { v ->
                    if (rel(u, v)) Edge(u, v) else null
                }
            }.toUnorderedFiniteSet()
        }

    /** RelationalGraph → Relation */
    fun <V> RelationalGraph<V>.toRelation(): Relation<V> =
        Relation { u, v -> Edge(u, v) in edges }

    /** RelationalGraph → Matrix(F₂) */
    fun <V> RelationalGraph<V>.toMatrix(): Matrix<Int> {
        val ordered = vertices.toOrderedFiniteSet()
        val n = ordered.size
        val data = List(n) { i ->
            List(n) { j ->
                if (Edge(ordered[i], ordered[j]) in edges) 1 else 0
            }
        }
        return Matrix(n, n, F2, data)
    }

    /** Matrix(F₂) → RelationalGraph<Int> */
    fun Matrix<Int>.toRelationalGraph(): RelationalGraph<Int> {
        require(rows == cols) { "Matrix must be square, but has dimensions $rows × $cols" }
        val n = rows
        val vertices = (0 until n).toOrderedFiniteSet()
        val edges = buildList {
            for (i in 0 until n)
                for (j in 0 until n)
                    if (get(i, j) == 1) add(Edge(i, j))
        }.toUnorderedFiniteSet()

        return object : RelationalGraph<Int> {
            override val vertices = vertices
            override val edges = edges
        }
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
