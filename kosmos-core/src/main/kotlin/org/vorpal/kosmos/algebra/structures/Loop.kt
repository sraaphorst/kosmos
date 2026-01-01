package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A [Loop] is a [Quasigroup] with an identity element over the operation, i.e. a [NonAssociativeMonoid].
 */
interface Loop<A : Any> : Quasigroup<A>, NonAssociativeMonoid<A> {
    companion object {
        fun <A : Any> of(
            op: BinOp<A>,
            identity: A,
            leftDiv: BinOp<A>,
            rightDiv: BinOp<A>
        ): Loop<A> = object : Loop<A> {
            override val op = op
            override val identity = identity
            override val leftDiv = leftDiv
            override val rightDiv = rightDiv
        }
    }
}
