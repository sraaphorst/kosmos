package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A [Semigroup], i.e. an associative structure with an identity element.
 *
 * It adds associativity to a [NonAssociativeMonoid], so it extends that as well.
 *
 * Laws:
 * 1. Associativity (from [Semigroup])
 * 2. Identity is both left and right unit: `op(identity, x) == x == op(x, identity)`.
 */
interface Monoid<A : Any> : Semigroup<A>, NonAssociativeMonoid<A> {
    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ): Monoid<A> = object : Monoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}
