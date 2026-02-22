package org.vorpal.kosmos.bridge

import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras.F2
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toOrderedFiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.DirectedEdge
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * A functorial bridge between:
 * - finite binary relations;
 * - directed graphs; and
 * - square adjacency matrices over the field `𝔽₂`.
 *
 * Notes:
 * - Our DirectedGraph representation forbids loops, so we ignore / reject diagonal 1s.
 * - DenseMat does not carry a field witness, so “over 𝔽₂” is enforced by checking entries in {0,1}.
 */
object RelationGraphMatrixBridge {

    private val ZERO: BigInteger = F2.add.identity
    private val ONE: BigInteger = F2.mul.identity

    /** Relation → DirectedGraph (ignores diagonal to avoid loops). */
    fun <V : Any> Relation<V>.toRelationalGraph(elements: FiniteSet<V>): DirectedGraph<V> {
        val vertices = elements.toUnordered()
        val edges =
            vertices
                .flatMap { u ->
                    vertices
                        .filter { v -> u != v && rel(u, v) }
                        .map { v -> DirectedEdge(u, v) }
                }
                .toUnorderedFiniteSet()

        return AdjacencySetDirectedGraph.of(vertices, edges)
    }

    /** DirectedGraph → Relation (out-neighbor relation). */
    fun <V : Any> DirectedGraph<V>.toRelation(): Relation<V> =
        Relation { u, v -> v in this.neighbors(u) }

    /** DirectedGraph → DenseMat(𝔽₂) adjacency matrix, using an ordering of vertices. */
    fun <V : Any> DirectedGraph<V>.toMatrix(): DenseMat<BigInteger> {
        val ordered = vertices.toOrderedFiniteSet()
        val n = ordered.size

        return DenseMat.tabulate(n, n) { i, j ->
            if (i == j) {
                ZERO
            } else {
                val e = DirectedEdge(ordered[i], ordered[j])
                if (e in edges) ONE else ZERO
            }
        }
    }

    /** DenseMat(𝔽₂) → DirectedGraph<Int>. Requires square, diagonal 0, and entries in {0,1}. */
    private fun isF2Entry(a: BigInteger): Boolean =
        a == ZERO || a == ONE

    fun DenseMat<BigInteger>.toRelationalGraphF2(): DirectedGraph<Int> {
        require(rows == cols) { "Matrix must be square, but has dimensions $rows${Symbols.TIMES}$cols" }
        require((0 until rows).all { i ->
            (0 until cols).all { j ->
                val a = this[i, j]
                isF2Entry(a) && (i != j || a == ZERO)
            }
        }) { "Matrix must have entries in {0,1} and diagonal must be 0 (no loops)." }

        val vertices = (0 until rows).toUnorderedFiniteSet()
        val edges =
            (0 until rows).flatMap { i ->
                (0 until cols).mapNotNull { j ->
                    if (i != j && this[i, j] == ONE) DirectedEdge(i, j) else null
                }
            }.toUnorderedFiniteSet()

        return AdjacencySetDirectedGraph.of(vertices, edges)
    }

    /** DenseMat(𝔽₂) → Relation<Int>. Requires square and entries in {0,1}. Diagonal allowed in relation. */
    fun DenseMat<BigInteger>.toRelationF2(): Relation<Int> {
        require(rows == cols) { "Matrix must be square, but has dimensions $rows${Symbols.TIMES}$cols" }
        require((0 until rows).all { i ->
            (0 until cols).all { j ->
                isF2Entry(this[i, j])
            }
        }) { "Matrix must have entries in {0,1} over 𝔽₂." }

        return Relation { i0, j0 -> this[i0, j0] == ONE }
    }

    /** Relation → DenseMat(𝔽₂) adjacency matrix (diagonal included if relation has it). */
    fun <V : Any> Relation<V>.toMatrix(elements: FiniteSet<V>): DenseMat<BigInteger> {
        val ordered = elements.toOrderedFiniteSet()
        val n = ordered.size

        return DenseMat.tabulate(n, n) { i, j ->
            if (rel(ordered[i], ordered[j])) ONE else ZERO
        }
    }
}
