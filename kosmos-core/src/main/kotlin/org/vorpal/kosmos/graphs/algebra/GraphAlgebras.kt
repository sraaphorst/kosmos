package org.vorpal.kosmos.graphs.algebra

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.AdjacencySetUndirectedGraph
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.graphs.UndirectedGraph
import org.vorpal.kosmos.graphs.connect
import org.vorpal.kosmos.graphs.meetOn


object UndirectedConnectCommutativeMonoid {
    fun <V: Any> instance(): CommutativeMonoid<UndirectedGraph<V>> = object :
        CommutativeMonoid<UndirectedGraph<V>> {
        override val identity: UndirectedGraph<V> = AdjacencySetUndirectedGraph.Companion.empty()
        override val op: BinOp<UndirectedGraph<V>> = BinOp(Symbols.BOWTIE) { x, y -> x connect y }
    }
}

object DirectedConnectMonoid {
    fun <V: Any> instance(): Monoid<DirectedGraph<V>> = object : Monoid<DirectedGraph<V>> {
        override val identity: DirectedGraph<V> = AdjacencySetDirectedGraph.Companion.empty()
        override val op: BinOp<DirectedGraph<V>> = BinOp(Symbols.BOWTIE) { x, y -> x connect y }
    }
}

/* =========================
 *  Overlay monoid instances
 * =========================*/

object UndirectedOverlayCommutativeMonoid {
    fun <V: Any> instance(): CommutativeMonoid<UndirectedGraph<V>> = object :
        CommutativeMonoid<UndirectedGraph<V>> {
        override val identity: UndirectedGraph<V> =
            AdjacencySetUndirectedGraph.Companion.edgeless(FiniteSet.Companion.empty())
        override val op: BinOp<UndirectedGraph<V>> =
            BinOp(Symbols.PLUS) { x, y -> x overlay y }
    }
}

object DirectedOverlayCommutativeMonoid {
    fun <V : Any> instance(): CommutativeMonoid<DirectedGraph<V>> = object : CommutativeMonoid<DirectedGraph<V>> {
        override val identity: DirectedGraph<V> =
            AdjacencySetDirectedGraph.Companion.edgeless(FiniteSet.Companion.empty())
        override val op: BinOp<DirectedGraph<V>> =
            BinOp(Symbols.PLUS) { x, y -> x overlay y }
    }
}

/* ===========================================
 *  Meet monoids on a fixed universe (with 1)
 * =========================================== */

private fun <V : Any> completeUndirected(vertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> =
    AdjacencySetUndirectedGraph.Companion.complete(vertices)

/** Meet monoid on a fixed universe: identity = complete graph K_universe. */
object UndirectedMeetCommutativeMonoid {
    fun <V : Any> on(universe: FiniteSet<V>): CommutativeMonoid<UndirectedGraph<V>> = object :
        CommutativeMonoid<UndirectedGraph<V>> {
        override val identity: UndirectedGraph<V> = completeUndirected(universe)
        override val op: BinOp<UndirectedGraph<V>> = BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
    }
}

/** Meet monoid on a fixed universe, identity = complete digraph (no loops) on universe. */
object DirectedMeetCommutativeMonoid {
    fun <V : Any> on(universe: FiniteSet<V>): CommutativeMonoid<DirectedGraph<V>> = object :
        CommutativeMonoid<DirectedGraph<V>> {
        override val identity: DirectedGraph<V> = AdjacencySetDirectedGraph.Companion.complete(universe)
        override val op: BinOp<DirectedGraph<V>> = BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
    }
}

/* ============================
 *  Idempotent graph semirings
 * ============================ */

object UndirectedCommutativeSemiring {
    /** (+,*) = (overlay, connect); zero = empty (no vertices). */
    fun <V : Any> instance(): CommutativeSemiring<UndirectedGraph<V>> = object :
        CommutativeSemiring<UndirectedGraph<V>> {
        override val add: CommutativeMonoid<UndirectedGraph<V>> =
            UndirectedOverlayCommutativeMonoid.instance()
        override val mul: CommutativeMonoid<UndirectedGraph<V>> =
            UndirectedConnectCommutativeMonoid.instance()
    }
}

object DirectedSemiring {
    /** (+,*) = (overlay, connect); zero = empty (no vertices). */
    fun <V : Any> instance(): Semiring<DirectedGraph<V>> = object : Semiring<DirectedGraph<V>> {
        override val add: CommutativeMonoid<DirectedGraph<V>> =
            DirectedOverlayCommutativeMonoid.instance()
        override val mul: Monoid<DirectedGraph<V>> =
            DirectedConnectMonoid.instance()
    }
}
