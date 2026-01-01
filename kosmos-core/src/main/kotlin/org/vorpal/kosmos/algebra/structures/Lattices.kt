package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.relations.Poset
import org.vorpal.kosmos.core.relations.Posets
import org.vorpal.kosmos.core.relations.Relation

/**
 * Join-semilattice `(A, ∨)`.
 *
 * Laws: associative, commutative, idempotent.
 *
 * Induces an order by:
 *
 *     a ≤_∨ b  ⇔  a ∨ b = b
 *
 * but "="-ness is provided by an Eq<A> at use-site (strict, approx, etc).
 */
interface JoinSemilattice<A : Any> {
    val join: BinOp<A>

    companion object {
        fun <A : Any> of(join: BinOp<A>): JoinSemilattice<A> =
            object : JoinSemilattice<A> {
                override val join = join
            }
    }
}

/**
 * Meet-semilattice `(A, ∧)`.
 *
 * Laws: associative, commutative, idempotent.
 *
 * Induces an order by:
 *
 *    a ≤_∧ b  ⇔  a ∧ b = a
 *
 * but "="-ness is provided by an Eq<A> at use-site.
 */
interface MeetSemilattice<A : Any> {
    val meet: BinOp<A>

    companion object {
        fun <A : Any> of(meet: BinOp<A>): MeetSemilattice<A> =
            object : MeetSemilattice<A> {
                override val meet = meet
            }
    }
}

/**
 * Lattice = join- and meet-semilattice satisfying absorption:
 *
 *    x ∧ (x ∨ y) = x
 *    x ∨ (x ∧ y) = x
 *
 * We do NOT bake an Eq into the structure. Any induced order/poset
 * is derived relative to an Eq<A> supplied at the call site.
 */
interface Lattice<A : Any> : JoinSemilattice<A>, MeetSemilattice<A> {

    /** Canonical order (we choose the join-induced order). */
    fun le(eq: Eq<A>, a: A, b: A): Boolean =
        this.leFromJoin(eq, a, b)

    /** Convenience: strict by default. */
    fun le(a: A, b: A): Boolean =
        le(Eq.default(), a, b)

    /** Canonical poset (join-induced), relative to Eq. */
    fun poset(eq: Eq<A>): Poset<A> =
        this.posetFromJoin(eq)

    /** Convenience: strict by default. */
    fun poset(): Poset<A> =
        poset(Eq.default())

    companion object {
        fun <A : Any> of(
            join: BinOp<A>,
            meet: BinOp<A>
        ): Lattice<A> = object : Lattice<A> {
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
        ): BoundedLattice<A> = object : BoundedLattice<A> {
            override val join = join
            override val meet = meet
            override val bottom = bottom
            override val top = top
        }
    }
}

/** Marker interface (laws check distributivity). */
interface DistributiveLattice<A : Any> : BoundedLattice<A> {
    companion object {
        fun <A : Any> of(
            join: BinOp<A>,
            meet: BinOp<A>,
            bottom: A,
            top: A
        ): DistributiveLattice<A> = object : DistributiveLattice<A> {
            override val join = join
            override val meet = meet
            override val bottom = bottom
            override val top = top
        }
    }
}

/* ============================================================================
 * Derived helpers
 * ========================================================================== */

/** a ≤_∨ b  ⇔  a ∨ b = b (relative to Eq). */
fun <A : Any> JoinSemilattice<A>.leFromJoin(eq: Eq<A>, a: A, b: A): Boolean =
    eq(join(a, b), b)

/** Join-induced poset (relative to Eq). */
fun <A : Any> JoinSemilattice<A>.posetFromJoin(eq: Eq<A>): Poset<A> =
    Posets.of(Relation(Symbols.LESS_THAN_EQ) { a, b -> leFromJoin(eq, a, b) })

/** a ≤_∧ b  ⇔  a ∧ b = a (relative to Eq). */
fun <A : Any> MeetSemilattice<A>.leFromMeet(eq: Eq<A>, a: A, b: A): Boolean =
    eq(meet(a, b), a)

/** Meet-induced poset (relative to Eq). */
fun <A : Any> MeetSemilattice<A>.posetFromMeet(eq: Eq<A>): Poset<A> =
    Posets.of(Relation(Symbols.LESS_THAN_EQ) { a, b -> leFromMeet(eq, a, b) })