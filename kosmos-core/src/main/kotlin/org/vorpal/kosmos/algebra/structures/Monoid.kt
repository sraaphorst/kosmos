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

/**
 * A commutative [Monoid].
 *
 * These add no properties to monoid, but come up often enough
 * that we include them as a means of tagging the monoid as commutative.
 *
 * [AbelianGroup] implements this.
 */
interface CommutativeMonoid<A: Any> : Monoid<A> {
    companion object {
        const val DEFAULT_SYMBOL = Symbols.DIAMOND_BIG

        fun <A: Any> of(
            identity: A,
            symbol: String = DEFAULT_SYMBOL,
            op: (A, A) -> A,
        ) : CommutativeMonoid<A> = object : CommutativeMonoid<A> {
            override val identity: A get() = identity
            override val op: BinOp<A> = BinOp(symbol, op)
        }
    }
}
