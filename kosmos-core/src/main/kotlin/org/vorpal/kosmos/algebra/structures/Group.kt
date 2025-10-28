package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

/**
 * A Group can be considered:
 * A Monoid with inverses.
 * A Loop with inverses.
 * Since a Group is a Loop, which is a Quasigroup, we satisfy the Quasigroup operations here.
 */
interface Group<A: Any> : Monoid<A>, Loop<A> {
    val inverse: (A) -> A
    override fun leftDiv(a: A, b: A): A = op(inverse(a), b)
    override fun rightDiv(b: A, a: A): A = op(b, inverse(a))

    companion object {
        const val DEFAULT_SYMBOL = Symbols.DOT

        fun <A: Any> of(
            identity: A,
            inverse: (A) -> A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
        ): Group<A> = object : Group<A> {
            override val identity = identity
            override val inverse: (A) -> A = inverse
            override val op: BinOp<A> = BinOp(symbol, op)
        }
    }
}

/**
 * Since AbelianGroups are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Group even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface AbelianGroup<A: Any> : Group<A> {
    companion object {
        const val DEFAULT_SYMBOL = Symbols.PLUS

        fun <A: Any> of(
            identity: A,
            inverse: (A) -> A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
        ): AbelianGroup<A> = object : AbelianGroup<A> {
            override val identity = identity
            override val inverse: (A) -> A = inverse
            override val op: BinOp<A> = BinOp(symbol, op)
        }
    }
}
