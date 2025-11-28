package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A NonAssociativeMonoid is an extension of a [NonAssociativeSemigroup] but with
 * an identity element.
 */
interface NonAssociativeMonoid<A : Any> : NonAssociativeSemigroup<A> {
    val identity: A

    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ) : NonAssociativeMonoid<A> = object : NonAssociativeMonoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}
