package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * A Group can be considered:
 * A Monoid with inverses.
 * A Loop with inverses.
 * Since a Group is a Loop, which is a Quasigroup, we satisfy the Quasigroup operations here.
 */
interface Group<A: Any> : Monoid<A>, Loop<A> {
    val inverse: Endo<A>
    override fun leftDiv(a: A, b: A) = op(inverse(a), b)
    override fun rightDiv(a: A, b: A) = op(b, inverse(a))

    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>,
            inverse: Endo<A>
        ): Group<A> = object : Group<A> {
            override val identity = identity
            override val op = op
            override val inverse = inverse
        }
    }
}

/**
 * Since AbelianGroups are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Group even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface AbelianGroup<A: Any> : Group<A>, CommutativeMonoid<A> {
    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>,
            inverse: Endo<A>
        ): AbelianGroup<A> = object : AbelianGroup<A> {
            override val identity = identity
            override val op = op
            override val inverse = inverse
        }
    }
}
