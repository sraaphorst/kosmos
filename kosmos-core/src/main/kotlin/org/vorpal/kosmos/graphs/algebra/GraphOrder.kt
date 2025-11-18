package org.vorpal.kosmos.graphs.algebra

import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.relations.Poset
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.AdjacencySetUndirectedGraph
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.graphs.UndirectedGraph
import org.vorpal.kosmos.graphs.complementOn

/* ---------- Subgraph order (variable vertex sets) ---------- */

object UndirectedGraphSubsetPoset {
    fun <V: Any> instance(): Poset<UndirectedGraph<V>> = object : Poset<UndirectedGraph<V>> {
        override val le: Relation<UndirectedGraph<V>> = Relation { x, y ->
            x.vertices.all { it in y.vertices } && x.edges.all { it in y.edges }
        }
    }
}

object DirectedGraphSubsetPoset {
    fun <V: Any> instance(): Poset<DirectedGraph<V>> = object : Poset<DirectedGraph<V>> {
        override val le: Relation<DirectedGraph<V>> = Relation { x, y ->
            x.vertices.all { it in y.vertices } && x.edges.all { it in y.edges }
        }
    }
}

/* ---------- Lattices under subgraph order ---------- */
/* join = overlay; meet = vertex ∩ + edge ∩ restricted to that vertex set. */

object UndirectedGraphLattice {
    fun <V: Any> instance(): Lattice<UndirectedGraph<V>> = object : Lattice<UndirectedGraph<V>> {
        override val join: BinOp<UndirectedGraph<V>> = BinOp { x, y -> x overlay y }
        override val meet: BinOp<UndirectedGraph<V>> = BinOp { x, y ->
            val v = (x.vertices intersect y.vertices).toUnorderedFiniteSet()
            val e = (x.edges intersect y.edges)
                .filter { e -> (e.u in v) && (e.v in v ) }
                .toUnorderedFiniteSet()
            AdjacencySetUndirectedGraph.of(v, e)
        }
    }
}

object DirectedGraphLattice {
    fun <V: Any> instance(): Lattice<DirectedGraph<V>> = object : Lattice<DirectedGraph<V>> {
        override val join: BinOp<DirectedGraph<V>> = BinOp { x, y -> x overlay y }
        override val meet: BinOp<DirectedGraph<V>> = BinOp { x, y ->
            val v = (x.vertices intersect y.vertices).toUnorderedFiniteSet()
            val e = (x.edges intersect y.edges)
                .filter { e -> (e.from in v) && (e.to in v ) }
                .toUnorderedFiniteSet()
            AdjacencySetDirectedGraph.of(v, e)
        }
    }
}

/* ---------- Boolean algebra on a fixed vertex universe U ---------- */
/* Requires complement w.r.t. U. We “promote” graphs to U by adding missing isolated vertices. */

object UndirectedBooleanAlgebraOn {
    fun <V: Any> of(universe: FiniteSet.Unordered<V>): BooleanAlgebra<UndirectedGraph<V>> =
        object : BooleanAlgebra<UndirectedGraph<V>> {

            private fun coerceToU(g: UndirectedGraph<V>): UndirectedGraph<V> {
                require(g.vertices.all { it in universe }) {
                    "Graph contains vertices outside the universe."
                }
                // Add missing isolated vertices so V(g)=U
                val promoted = AdjacencySetUndirectedGraph.of(universe, g.edges)
                return promoted
            }

            override val bottom = AdjacencySetUndirectedGraph.edgeless(universe)
            override val top = AdjacencySetUndirectedGraph.complete(universe)


            override val join: BinOp<UndirectedGraph<V>> = BinOp { x, y ->
                (coerceToU(x) overlay coerceToU(y))
            }

            override val meet: BinOp<UndirectedGraph<V>> = BinOp { x, y ->
                val xp = coerceToU(x)
                val yp = coerceToU(y)
                val e = (xp.edges intersect yp.edges).toUnorderedFiniteSet()
                AdjacencySetUndirectedGraph.of(universe, e)
            }

            // ensure V(x) ⊆ universe (and no stray vertices).
            override val not: Endo<UndirectedGraph<V>> = Endo { x -> coerceToU(x).complementOn(universe) }
        }
}
