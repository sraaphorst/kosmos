package org.vorpal.kosmos.graphs.core

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.relations.Relation

/**
 * Let A = V be a set of size n.
 * - The set of binary relations B = (A, R ⊆ A × A);
 * - The set of directed simple graphs permitting loops, say D = (V, E ⊆ V × V); and
 * - (If the elements of A = V have a fixed order) M_n(F_2).
 * This serves as a functional bridge between binary relations and this class of graphs.
 */
interface RelationalGraph<V> : Graph<V, Edge<V>> {
    override val vertices: FiniteSet.Ordered<V>
    val relation: Relation<V>
        get() = Relation { u, v -> Edge(u, v) in edges }
}
