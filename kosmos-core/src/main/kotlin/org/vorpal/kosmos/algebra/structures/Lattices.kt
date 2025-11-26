package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.relations.Poset
import org.vorpal.kosmos.core.relations.Posets
import org.vorpal.kosmos.core.relations.Relation

/**
 * Join-semilattice (A, ∨).
 *
 * Laws: associative, commutative, idempotent.
 *
 * Induces an order by a ≤_∨ b ⇔ a ∨ b = b.
 */
interface JoinSemilattice<A : Any> {
    val join: BinOp<A>

    companion object {
        fun <A : Any> of(
            join: BinOp<A>
        ): JoinSemilattice<A> =
            object : JoinSemilattice<A> {
                override val join = join
            }
    }
}

/**
 * Meet-semilattice (A, ∧).
 *
 * Laws: associative, commutative, idempotent.
 *
 * Induces an order by a ≤_∧ b ⇔ a ∧ b = a.
 */
interface MeetSemilattice<A : Any> {
    val meet: BinOp<A>

    companion object {
        fun <A : Any> of(
            meet: BinOp<A>
        ): MeetSemilattice<A> =
            object : MeetSemilattice<A> {
                override val meet = meet
            }
    }
}

/* -------- Derived helpers that avoid diamonds (different names) -------- */

fun <A : Any> JoinSemilattice<A>.leFromJoin(a: A, b: A): Boolean =
    join(a, b) == b

fun <A : Any> JoinSemilattice<A>.posetFromJoin(): Poset<A> =
    Posets.of(Relation(Symbols.LESS_THAN_EQ) { a, b -> leFromJoin(a, b) })

fun <A : Any> MeetSemilattice<A>.leFromMeet(a: A, b: A): Boolean =
    meet(a, b) == a

fun <A : Any> MeetSemilattice<A>.posetFromMeet(): Poset<A> =
    Posets.of(Relation(Symbols.LESS_THAN_EQ) { a, b -> leFromMeet(a, b) })

/**
 * Lattice = join- and meet-semilattice satisfying the absorption laws:
 *   x ∧ (x ∨ y) = x  and  x ∨ (x ∧ y) = x.
 *
 * We pick the join-induced order as the canonical one; laws should ensure
 * it coincides with the meet-induced order.
 */
interface Lattice<A : Any> : JoinSemilattice<A>, MeetSemilattice<A> {
    /** Canonical order (we choose the join-induced order). */
    val poset: Poset<A>
        get() = posetFromJoin()

    /** Convenience: non-strict order. */
    fun le(a: A, b: A): Boolean = leFromJoin(a, b)

    companion object {
        fun <A : Any> of(join: BinOp<A>, meet: BinOp<A>): Lattice<A> =
            object : Lattice<A> {
                override val join = join
                override val meet = meet
            }
    }
}

/** Bounded lattice has ⊥ and ⊤ with ⊥ ≤ x ≤ ⊤. */
interface BoundedLattice<A : Any> : Lattice<A> {
    val bottom: A
    val top: A

    companion object {
        fun <A : Any> of(
            join: BinOp<A>,
            meet: BinOp<A>,
            bottom: A,
            top: A
        ): BoundedLattice<A> =
            object : BoundedLattice<A> {
                override val join = join
                override val meet = meet
                override val bottom = bottom
                override val top = top
            }
    }
}

/** Marker interface (laws check distributivity). */
interface DistributiveLattice<A : Any> : BoundedLattice<A>
