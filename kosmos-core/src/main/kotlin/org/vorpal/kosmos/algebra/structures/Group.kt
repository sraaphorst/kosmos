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
interface Group<A : Any> : Monoid<A>, Loop<A> {
    val inverse: Endo<A>

    // Artifacts to be taken care of since we inherit from Quasigroup through Loop.
    override val leftDiv: BinOp<A>
        get() = BinOp(Symbols.DIV_LEFT) { a, b -> op(inverse(a), b) }
    override val rightDiv: BinOp<A>
        get() = BinOp(Symbols.DIV_RIGHT) { a, b -> op(b, inverse(a)) }

    companion object {
        fun <A : Any> of(
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
