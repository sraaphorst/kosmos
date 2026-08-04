package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * An abstraction of a general graph, containing a set of vertices.
 */
sealed interface Graph<V: Any> {
    /**
     * The vertices in the graph.
     */
    val vertices: FiniteSet.Unordered<V>

    /**
     * The number of vertices in the graph (the *order* of the graph, `|V|`).
     *
     * Usage rule: use [vertexCount] (and [edgeCount]) when asking *the graph* a
     * question — implicit/rule-defined graphs can answer analytically, without
     * materializing anything, and the answer may exceed [Int] range. Plain
     * `.size` remains correct when comparing collections an algorithm has
     * itself materialized (e.g. a BFS distance map against the vertex set it
     * was filled from, or a capacity hint): such code is materialization-bound
     * by construction, and the `.size` marks it as such.
     */
    val vertexCount: Long
        get() = vertices.size.toLong()

    /**
     * The number of edges in the graph (the *size* of the graph, `|E|`).
     *
     * The edge set does not have to be explicitly built or known: implicit
     * graphs override this with a closed form (e.g. `n(n-1)/2` for `K_n`),
     * which is why the type is [Long] — such counts overflow [Int] long before
     * the graph itself becomes unrepresentable.
     *
     * See [vertexCount] for the `edgeCount`-vs-`edges.size` usage rule.
     */
    val edgeCount: Long
}

/**
 * Vertices that arise from the disjoint sum of two graphs.
 */
sealed interface SumVertex<A, B> {
    data class InLeft<A, B>(val value: A) : SumVertex<A, B>
    data class InRight<A, B>(val value: B) : SumVertex<A, B>
}
