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
    override fun leftDiv(a: A, b: A): A = op(inverse(a), b)
    override fun rightDiv(b: A, a: A): A = op(b, inverse(a))

    companion object {
        const val DEFAULT_SYMBOL = Symbols.DOT
        const val INVERSE_SYMBOL = Symbols.INVERSE

        fun <A: Any> of(
            identity: A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
            inverseSymbol: String = INVERSE_SYMBOL,
            inverseOp: (A) -> A,
        ): Group<A> = object : Group<A> {
            override val identity = identity
            override val inverse: Endo<A> = Endo(inverseSymbol, inverseOp)
            override val op: BinOp<A> = BinOp(symbol, op)
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
        const val DEFAULT_SYMBOL = Symbols.PLUS
        const val INVERSE_SYMBOL = Symbols.MINUS

        fun <A: Any> of(
            identity: A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
            inverseOp: (A) -> A,
            inverseSymbol: String = INVERSE_SYMBOL,
        ): AbelianGroup<A> = object : AbelianGroup<A> {
            override val identity = identity
            override val inverse: Endo<A> = Endo(inverseSymbol, inverseOp)
            override val op: BinOp<A> = BinOp(symbol, op)
        }
    }
}
