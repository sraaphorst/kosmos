package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet

// ---------- Induced restriction (just V ∩ universe) ----------

/** Induced subgraph on V ∩ [universe]. */
fun <V: Any> UndirectedGraph<V>.restrict(universe: FiniteSet<V>): UndirectedGraph<V> {
    val subV = (vertices intersect universe).toUnorderedFiniteSet()
    return inducedSubgraph(subV)
}

/** Induced subgraph on V ∩ [universe]. */
fun <V: Any> DirectedGraph<V>.restrict(universe: FiniteSet<V>): DirectedGraph<V> {
    val subV = (vertices intersect universe).toUnorderedFiniteSet()
    return inducedSubgraph(subV)
}

// ---------- Embed into a fixed universe (add missing isolated vertices) ----------

/**
 * Make the vertex set exactly [universe]: restrict to [universe] and add
 * any missing vertices as isolated vertices. Useful when you want laws
 * over a *fixed* universe (e.g., meet monoid with identity K_universe).
 */
fun <V: Any> UndirectedGraph<V>.embedIntoUniverse(
    universe: FiniteSet<V>,
    factory: (FiniteSet<V>) -> UndirectedGraph<V> = { vs -> AdjacencySetUndirectedGraph.edgeless(vs) }
): UndirectedGraph<V> {
    val subV = (vertices intersect universe).toUnorderedFiniteSet()
    val g = inducedSubgraph(subV)
    val missing = (universe - subV).toUnorderedFiniteSet()
    return if (missing.isEmpty) g else g overlay factory(missing)
}

/** Directed analogue: add missing vertices as isolated (no in/out arcs). */
fun <V: Any> DirectedGraph<V>.embedIntoUniverse(
    universe: FiniteSet<V>,
    factory: (FiniteSet<V>) -> DirectedGraph<V> = { vs -> AdjacencySetDirectedGraph.edgeless(vs) }
): DirectedGraph<V> {
    val subV = (vertices intersect universe).toUnorderedFiniteSet()
    val g = inducedSubgraph(subV)
    val missing = (universe - subV).toUnorderedFiniteSet()
    return if (missing.isEmpty) g else g overlay factory(missing)
}