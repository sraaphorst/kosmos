package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

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
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ) : CommutativeMonoid<A> = object : CommutativeMonoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}
