package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A Group can be considered:
 * A Monoid with inverses.
 * A Loop with inverses.
 * Since a Group is a Loop, which is a Quasigroup, we satisfy the Quasigroup operations here.
 */
interface Group<A> : Monoid<A>, Loop<A> {
    val inv: (A) -> A
    override fun leftDiv(a: A, b: A): A = inv(a).let { op.combine(it, b) }
    override fun rightDiv(b: A, a: A): A = inv(a).let { op.combine(b, it) }

    companion object {
        fun <A> of(op: (A, A) -> A, identity: A, inv: (A) -> A): Group<A> = object : Group<A> {
            override val op: BinOp<A> = BinOp(op)
            override val identity = identity
            override val inv: (A) -> A = inv
        }
    }
}

/**
 * Since AbelianGroups are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Group even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface AbelianGroup<A> : Group<A> {
    companion object {
        fun <A> of(op: (A, A) -> A, identity: A, inv: (A) -> A): AbelianGroup<A> = object : AbelianGroup<A> {
            override val op: BinOp<A> = BinOp(op)
            override val identity = identity
            override val inv: (A) -> A = inv
        }
    }
}
