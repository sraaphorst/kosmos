package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A semigroup with commutativity.
 */
interface CommutativeSemigroup<A : Any> : Semigroup<A> {
    companion object {
        fun <A : Any> of(
            op: BinOp<A>
        ): CommutativeSemigroup<A> = object : CommutativeSemigroup<A> {
            override val op = op
        }
    }
}