package org.vorpal.kosmos.graphs.algebra

import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Lattice
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.relations.Poset
import org.vorpal.kosmos.core.relations.Posets
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.AdjacencySetUndirectedGraph
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.graphs.UndirectedGraph
import org.vorpal.kosmos.graphs.complementOn
import org.vorpal.kosmos.graphs.connect
import org.vorpal.kosmos.graphs.meetOn

object GraphAlgebras {
    object Structures {

        /* =========================
         *  Connect monoid instances
         * =========================*/

        fun <V : Any> undirectedConnectCommutativeMonoid(): CommutativeMonoid<UndirectedGraph<V>> =
            CommutativeMonoid.of(
                AdjacencySetUndirectedGraph.empty(),
                BinOp(Symbols.BOWTIE) { x, y -> x connect y }
            )

        fun <V : Any> directedConnectMonoid(): Monoid<DirectedGraph<V>> = Monoid.of(
            AdjacencySetDirectedGraph.empty(),
            BinOp(Symbols.BOWTIE) { x, y -> x connect y }
        )

        /* =========================
         *  Overlay monoid instances
         * =========================*/

        fun <V : Any> undirectedOverlayCommutativeMonoid(): CommutativeMonoid<UndirectedGraph<V>> =
            CommutativeMonoid.of(
                AdjacencySetUndirectedGraph.empty(),
                BinOp(Symbols.PLUS) { x, y -> x overlay y }
            )

        fun <V : Any> directedOverlayCommutativeMonoid(): CommutativeMonoid<DirectedGraph<V>> = CommutativeMonoid.of(
            AdjacencySetDirectedGraph.empty(),
            BinOp(Symbols.PLUS) { x, y -> x overlay y }
        )

        /* ===========================================
         *  Meet monoids on a fixed universe (with 1)
         * =========================================== */

        private fun <V : Any> completeUndirected(vertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> =
            AdjacencySetUndirectedGraph.complete(vertices)

        /** Meet monoid on a fixed universe: identity = complete graph K_universe. */
        fun <V : Any> undirectedMeetCommutativeMonoid(universe: FiniteSet<V>): CommutativeMonoid<UndirectedGraph<V>> =
            CommutativeMonoid.of(
                completeUndirected(universe),
                BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
            )

        /** Meet monoid on a fixed universe, identity = complete digraph (no loops) on universe. */
        fun <V : Any> directedMeetCommutativeMonoid(universe: FiniteSet<V>): CommutativeMonoid<DirectedGraph<V>> =
            CommutativeMonoid.of(
                AdjacencySetDirectedGraph.complete(universe),
                BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
            )

        /* ============================
         *  Idempotent graph semirings
         * ============================ */

        fun <V : Any> undirectedCommutativeSemiring(): CommutativeSemiring<UndirectedGraph<V>> = CommutativeSemiring.of(
            undirectedOverlayCommutativeMonoid(),
            undirectedConnectCommutativeMonoid(),
        )

        fun <V : Any> directedSemiring(): Semiring<DirectedGraph<V>> = Semiring.of(
            directedOverlayCommutativeMonoid(),
            directedConnectMonoid()
        )
    }

    object Orders {
        fun <V : Any> undirectedGraphSubsetPoset(): Poset<UndirectedGraph<V>> = Posets.of(
            Relation { x, y ->
                x.vertices.all { it in y.vertices } && x.edges.all { it in y.edges }
            }
        )

        fun <V : Any> directedGraphSubsetPoset(): Poset<DirectedGraph<V>> = Posets.of(
            Relation { x, y ->
                x.vertices.all { it in y.vertices } && x.edges.all { it in y.edges }
            }
        )
    }

    object Lattices {
        fun <V : Any> undirectedLattice(): Lattice<UndirectedGraph<V>> = Lattice.of(
            BinOp { x, y -> x overlay y },
            BinOp { x, y ->
                val v = (x.vertices intersect y.vertices).toUnorderedFiniteSet()
                val e = (x.edges intersect y.edges)
                    .filter { e -> (e.u in v) && (e.v in v) }
                    .toUnorderedFiniteSet()
                AdjacencySetUndirectedGraph.of(v, e)
            }
        )

        fun <V : Any> directedGraphLattice(): Lattice<DirectedGraph<V>> = Lattice.of(
            BinOp { x, y -> x overlay y },
            BinOp { x, y ->
                val v = (x.vertices intersect y.vertices).toUnorderedFiniteSet()
                val e = (x.edges intersect y.edges)
                    .filter { e -> (e.from in v) && (e.to in v) }
                    .toUnorderedFiniteSet()
                AdjacencySetDirectedGraph.of(v, e)
            }
        )
    }

    object BooleanAlgebras {
        fun <V : Any> undirectedBooleanAlgebraOn(universe: FiniteSet.Unordered<V>): BooleanAlgebra<UndirectedGraph<V>> =
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
}