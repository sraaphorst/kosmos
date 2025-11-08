package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

/**
 * A Semigroup with an identity element.
 */
interface Monoid<A: Any> : Semigroup<A> {
    val identity: A

    companion object {
        const val DEFAULT_SYMBOL = Symbols.DIAMOND

        fun <A: Any> of(
            identity: A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
        ): Monoid<A> = object : Monoid<A> {
            override val identity: A get() = identity
            override val op: BinOp<A> = BinOp(symbol, op)
        }
    }
}
