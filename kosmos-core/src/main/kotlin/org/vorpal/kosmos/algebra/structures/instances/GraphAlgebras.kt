package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.graphs.AdjacencySetDirectedGraph
import org.vorpal.kosmos.graphs.AdjacencySetUndirectedGraph
import org.vorpal.kosmos.graphs.DirectedEdge
import org.vorpal.kosmos.graphs.DirectedGraph
import org.vorpal.kosmos.graphs.UndirectedEdge
import org.vorpal.kosmos.graphs.UndirectedGraph

object GraphAlgebras {
    /* =========================================
     *  Small helpers for fixed-UNIVERSE algebra
     * ========================================= */

    /** Induced subgraph on V ∩ [universe]. */
    infix fun <V : Any> UndirectedGraph<V>.restrict(universe: FiniteSet<V>): UndirectedGraph<V> {
        val subV = (vertices intersect universe).toUnorderedFiniteSet()
        return inducedSubgraph(subV)
    }

    /** Induced subgraph on V ∩ [universe]. */
    infix fun <V : Any> DirectedGraph<V>.restrict(universe: FiniteSet<V>): DirectedGraph<V> {
        val subV = (vertices intersect universe).toUnorderedFiniteSet()
        return inducedSubgraph(subV)
    }

    /**
     * Make the vertex set exactly [universe]: restrict to [universe] and add missing vertices as isolated.
     * Useful to get proper identities for meet/compl over a *fixed* carrier.
     */
    fun <V : Any> UndirectedGraph<V>.embedIntoUniverse(
        universe: FiniteSet<V>,
        factory: (FiniteSet<V>) -> UndirectedGraph<V> = { vs -> AdjacencySetUndirectedGraph.edgeless(vs) }
    ): UndirectedGraph<V> {
        val subV = (vertices intersect universe).toUnorderedFiniteSet()
        val g = inducedSubgraph(subV)
        val missing = (universe - subV).toUnorderedFiniteSet()
        return if (missing.isEmpty) g else g overlay factory(missing)
    }

    /** Directed analogue: add missing vertices as isolated (no in/out arcs). */
    fun <V : Any> DirectedGraph<V>.embedIntoUniverse(
        universe: FiniteSet<V>,
        factory: (FiniteSet<V>) -> DirectedGraph<V> = { vs -> AdjacencySetDirectedGraph.edgeless(vs) }
    ): DirectedGraph<V> {
        val subV = (vertices intersect universe).toUnorderedFiniteSet()
        val g = inducedSubgraph(subV)
        val missing = (universe - subV).toUnorderedFiniteSet()
        return if (missing.isEmpty) g else g overlay factory(missing)
    }

    /* ===================================================
     *  Undirected: meet (& meet-on-universe) + subgraph-of
     * =================================================== */

    /**
     * Edgewise meet (intersection). Vertex set is the union; edge set = E(G) ∩ E(H).
     *
     * Not a monoid by itself (no global identity across varying carriers).
     *
     * Note: This operation is commutative.
     */
    infix fun <V : Any> UndirectedGraph<V>.meet(other: UndirectedGraph<V>): UndirectedGraph<V> {
        val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
        val e = (this.edges intersect other.edges).toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(v, e)
    }

    /**
     * Meet *on a fixed* [universe]: identity = complete graph on [universe].
     *
     * NOTE: This operation is commutative.
     */
    fun <V : Any> UndirectedGraph<V>.meetOn(
        universe: FiniteSet<V>,
        other: UndirectedGraph<V>
    ): UndirectedGraph<V> {
        val gx = this.embedIntoUniverse(universe)
        val gy = other.embedIntoUniverse(universe)
        val e = (gx.edges intersect gy.edges).toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(universe.toUnorderedFiniteSet(), e)
    }

    /** Subgraph relation: V(G) ⊆ V(H) and E(G) ⊆ E(H). */
    infix fun <V : Any> UndirectedGraph<V>.isSubgraphOf(other: UndirectedGraph<V>): Boolean =
        this.vertices.all { it in other.vertices } && this.edges.all { it in other.edges }

    /* ======================================
     *  Directed: meet (& meet-on-universe)
     * ====================================== */

    /**
     * NOTE: that this operation is commutative.
     */
    infix fun <V : Any> DirectedGraph<V>.meet(other: DirectedGraph<V>): DirectedGraph<V> {
        val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
        val e = (this.edges intersect other.edges).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(v, e)
    }

    /**
     * NOTE: that this operation is commutative.
     */
    fun <V : Any> DirectedGraph<V>.meetOn(
        universe: FiniteSet<V>,
        other: DirectedGraph<V>
    ): DirectedGraph<V> {
        val gx = this.embedIntoUniverse(universe)
        val gy = other.embedIntoUniverse(universe)
        val e = (gx.edges intersect gy.edges).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(universe.toUnorderedFiniteSet(), e)
    }

    infix fun <V : Any> DirectedGraph<V>.isSubgraphOf(other: DirectedGraph<V>): Boolean =
        this.vertices.all { it in other.vertices } && this.edges.all { it in other.edges }

    /* ==============================
     *  Algebraic “connect” operator
     * ============================== */

    /**
     * Undirected connect: G ⋈ H (adds all cross edges {u,v} with u∈V(G), v∈V(H), u≠v) plus existing edges.
     *
     * If vertex sets overlap, you naturally densify across the overlap.
     *
     * NOTE: Because we are dealing with undirected graphs, this operation is commutative.
     */
    infix fun <V : Any> UndirectedGraph<V>.connect(other: UndirectedGraph<V>): UndirectedGraph<V> {
        val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
        val cross = this.vertices.flatMap { u ->
            other.vertices.mapNotNull { w ->
                if (u != w) UndirectedEdge(u, w) else null
            }
        }.toUnorderedFiniteSet()
        val e = (this.edges + other.edges + cross).toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(v, e)
    }

    object UndirectedConnectCommutativeMonoid {
        fun <V: Any> instance(): CommutativeMonoid<UndirectedGraph<V>> = object : CommutativeMonoid<UndirectedGraph<V>> {
            override val identity: UndirectedGraph<V> = AdjacencySetUndirectedGraph.empty()
            override val op: BinOp<UndirectedGraph<V>> = BinOp(Symbols.BOWTIE) { x, y -> x connect y }
        }
    }

    /**
     * Directed connect: adds all cross arcs u→v with u∈V(G), v∈V(H), u≠v, plus existing arcs.
     *
     * NOTE: This operation is NOT commutative since the arcs go from the left to right.
     */
    infix fun <V : Any> DirectedGraph<V>.connect(other: DirectedGraph<V>): DirectedGraph<V> {
        val v = (this.vertices + other.vertices).toUnorderedFiniteSet()
        val cross = this.vertices.flatMap { u ->
            other.vertices.mapNotNull { w ->
                if (u != w) DirectedEdge(u, w) else null
            }
        }.toUnorderedFiniteSet()
        val e = (this.edges + other.edges + cross).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(v, e)
    }

    object DirectedConnectMonoid {
        fun <V: Any> instance(): Monoid<DirectedGraph<V>> = object : Monoid<DirectedGraph<V>> {
            override val identity: DirectedGraph<V> = AdjacencySetDirectedGraph.empty()
            override val op: BinOp<DirectedGraph<V>> = BinOp(Symbols.BOWTIE) { x, y -> x connect y }
        }
    }

    /* =========================
     *  Overlay monoid instances
     * =========================*/

    object UndirectedOverlayCommutativeMonoid {
        fun <V: Any> instance(): CommutativeMonoid<UndirectedGraph<V>> = object : CommutativeMonoid<UndirectedGraph<V>> {
            override val identity: UndirectedGraph<V> =
                AdjacencySetUndirectedGraph.edgeless(FiniteSet.empty())
            override val op: BinOp<UndirectedGraph<V>> =
                BinOp(Symbols.PLUS) { x, y -> x overlay y }
        }
    }

    object DirectedOverlayCommutativeMonoid {
        fun <V : Any> instance(): CommutativeMonoid<DirectedGraph<V>> = object : CommutativeMonoid<DirectedGraph<V>> {
            override val identity: DirectedGraph<V> =
                AdjacencySetDirectedGraph.edgeless(FiniteSet.empty())
            override val op: BinOp<DirectedGraph<V>> =
                BinOp(Symbols.PLUS) { x, y -> x overlay y }
        }
    }

    /* ===========================================
     *  Meet monoids on a fixed universe (with 1)
     * =========================================== */

    private fun <V : Any> completeUndirected(vertices: FiniteSet<V>): AdjacencySetUndirectedGraph<V> =
        AdjacencySetUndirectedGraph.complete(vertices)

    /** Meet monoid on a fixed universe: identity = complete graph K_universe. */
    object UndirectedMeetCommutativeMonoid {
        fun <V : Any> on(universe: FiniteSet<V>): CommutativeMonoid<UndirectedGraph<V>> = object : CommutativeMonoid<UndirectedGraph<V>> {
            override val identity: UndirectedGraph<V> = completeUndirected(universe)
            override val op: BinOp<UndirectedGraph<V>> = BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
        }
    }

    /** Meet monoid on a fixed universe, identity = complete digraph (no loops) on universe. */
    object DirectedMeetCommutativeMonoid {
        fun <V : Any> on(universe: FiniteSet<V>): CommutativeMonoid<DirectedGraph<V>> = object : CommutativeMonoid<DirectedGraph<V>> {
            override val identity: DirectedGraph<V> = AdjacencySetDirectedGraph.complete(universe)
            override val op: BinOp<DirectedGraph<V>> = BinOp(Symbols.WEDGE) { x, y -> x.meetOn(universe, y) }
        }
    }

    /* ============================
     *  Idempotent graph semirings
     * ============================ */

    object UndirectedCommutativeSemiring {
        /** (+,*) = (overlay, connect); zero = empty (no vertices). */
        fun <V : Any> instance(): CommutativeSemiring<UndirectedGraph<V>> = object : CommutativeSemiring<UndirectedGraph<V>> {
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

    /* ==============================
     *  Extra graph products (quick)
     * ============================== */

    /**
     * Tensor / direct product (⊗): ((u1,v1),(u2,v2)) iff {u1,u2} ∈ E(G) && {v1,v2} ∈ E(H).
     */
    infix fun <V : Any, W : Any> UndirectedGraph<V>.tensorProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()
        val e = v.flatMap { (u1, v1) ->
            v.mapNotNull { (u2, v2) ->
                if (u1 != u2 || v1 != v2) {
                    val e1 = UndirectedEdge(u1, u2) in this.edges
                    val e2 = UndirectedEdge(v1, v2) in other.edges
                    if (e1 && e2) UndirectedEdge(u1 to v1, u2 to v2) else null
                } else null
            }
        }.toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(v, e)
    }

    /**
     * Directed tensor / direct / Kronecker product (⊗): ((u1,v1) → (u2,v2)) iff (u1, u2) ∈ G AND (v1, v2) ∈ H.
     */
    infix fun <V: Any, W: Any> DirectedGraph<V>.tensorProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()
        // Edges: for each arc in G and each arc in H, pair them.
        val e = edges.flatMap { (a, b) ->
            other.edges.map { (c, d) -> DirectedEdge(a to c, b to d) }
        }.toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(v, e)
    }

    /**
     * Strong product (⊠): edge if it’s an edge in at least one factor and equal in the other.
     */
    infix fun <V : Any, W : Any> UndirectedGraph<V>.strongProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()
        val e = v.flatMap { (u1, v1) ->
            v.mapNotNull { (u2, v2) ->
                if (u1 != u2 || v1 != v2) {
                    val gEdge = UndirectedEdge(u1, u2) in this.edges
                    val hEdge = UndirectedEdge(v1, v2) in other.edges
                    if ((gEdge && v1 == v2) || (hEdge && u1 == u2) || (gEdge && hEdge))
                        UndirectedEdge(u1 to v1, u2 to v2) else null
                } else null
            }
        }.toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(v, e)
    }

    /**
     * Directed strong product (⊠) = cartesianProduct □ plus tensorProduct ⊗.
     * ((u1,v1) → (u2,v2)) iff (u1 == u2 && v1 → v2) || (u1 → u2 && v1 == v2) || (u1 → u2 && v1 → v2)
     */
    infix fun <V: Any, W: Any> DirectedGraph<V>.strongProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()

        // Cartesian part: move in exactly one coordinate.
        val cart = buildList {
            // Fix G, move in H
            other.edges.forEach { (c, d) ->
                vertices.forEach { a -> add(DirectedEdge(a to c, a to d)) }
            }
            // Fix H, move in G
            edges.forEach { (a, b) ->
                other.vertices.forEach { c -> add(DirectedEdge(a to c, b to c)) }
            }
        }

        // Tensor part: move in both coordinates.
        val tens = edges.flatMap { (a, b) ->
            other.edges.map { (c, d) -> DirectedEdge(a to c, b to d) }
        }

        val e = (cart + tens).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(v, e)
    }

    /**
     * Lexicographic product (•): ((u1,v1), (u2,v2)) iff {u1,u2} ∈ E(G) || (u1==u2 && {v1,v2} ∈ E(H)).
     */
    infix fun <V : Any, W : Any> UndirectedGraph<V>.lexicographicProduct(other: UndirectedGraph<W>): UndirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()
        val e = v.flatMap { (u1, v1) ->
            v.mapNotNull { (u2, v2) ->
                if (u1 != u2 || v1 != v2) {
                    val gEdge = UndirectedEdge(u1, u2) in this.edges
                    val hEdge = UndirectedEdge(v1, v2) in other.edges
                    if (gEdge || (u1 == u2 && hEdge))
                        UndirectedEdge(u1 to v1, u2 to v2) else null
                } else null
            }
        }.toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(v, e)
    }

    /**
     * Directed lexicographic product (∘).
     * ((u1,v1) → (u2,v2)) iff (u1 → u2 ∈ G) || (u1 == u2 && v1 → v2 ∈ H).
     *
     * Intuition: each vertex of G is “replaced” by a copy of H; every arc u1 → u2 in G
     * induces all arcs from the entire copy at u1 to the entire copy at u2.
     */
    infix fun <V: Any, W: Any> DirectedGraph<V>.lexicographicProduct(other: DirectedGraph<W>): DirectedGraph<Pair<V, W>> {
        val v = vertices.cartesianProduct(other.vertices).toUnordered()

        // Across-fibers: for each arc a→b in G, connect every (a, *) to every (b, *).
        val across = edges.flatMap { (a, b) ->
            other.vertices.flatMap { c ->
                other.vertices.map { d -> DirectedEdge(a to c, b to d) }
            }
        }

        // Within-fibers: for each a in G, replicate H inside the fiber over a.
        val within = vertices.flatMap { a ->
            other.edges.map { (c, d) -> DirectedEdge(a to c, a to d) }
        }

        val e = (across + within).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(v, e)
    }

    /* ===================================
     *  Edge complements (fixed universe)
     * =================================== */

    /** Undirected edge-complement on a fixed [universe] (no multi-edges; loops excluded). */
    fun <V : Any> UndirectedGraph<V>.edgeComplementOn(universe: FiniteSet<V>): UndirectedGraph<V> {
        val g = this.embedIntoUniverse(universe)
        val all = completeUndirected(universe).edges
        val comp = (all - g.edges).toUnorderedFiniteSet()
        return AdjacencySetUndirectedGraph.of(universe.toUnorderedFiniteSet(), comp)
    }

    /** Directed edge-complement on a fixed [universe] (no loops). */
    fun <V : Any> DirectedGraph<V>.edgeComplementOn(universe: FiniteSet<V>): DirectedGraph<V> {
        val g = this.embedIntoUniverse(universe)
        val all = AdjacencySetDirectedGraph.complete(universe).edges
        val comp = (all - g.edges).toUnorderedFiniteSet()
        return AdjacencySetDirectedGraph.of(universe.toUnorderedFiniteSet(), comp)
    }

    /* =====================
     *  Graph homomorphisms
     * ===================== */

    fun <V : Any, W : Any> UndirectedGraph<V>.isHomomorphism(
        to: UndirectedGraph<W>,
        f: (V) -> W
    ): Boolean {
        if (!vertices.all { f(it) in to.vertices }) return false
        return edges.all { e -> UndirectedEdge(f(e.u), f(e.v)) in to.edges }
    }

    fun <V : Any, W : Any> DirectedGraph<V>.isHomomorphism(
        to: DirectedGraph<W>,
        f: (V) -> W
    ): Boolean {
        if (!vertices.all { f(it) in to.vertices }) return false
        return edges.all { e -> DirectedEdge(f(e.from), f(e.to)) in to.edges }
    }
}
