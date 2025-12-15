package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/** A quasigroup: for all a,b there exist unique x,y with a⋆x=b and y⋆a=b. */
interface Quasigroup<A : Any> : NonAssociativeSemigroup<A> {
    /** Left division: the unique x with a ⋆ x = b. */
    val leftDiv: BinOp<A>
    /** Right division: the unique y with y ⋆ a = b. */
    val rightDiv: BinOp<A>

    companion object {
        fun <A : Any> of(
            op: BinOp<A>,
            leftDiv: BinOp<A>,
            rightDiv: BinOp<A>
        ): Quasigroup<A> = object : Quasigroup<A> {
            override val op = op
            override val leftDiv = leftDiv
            override val rightDiv = rightDiv
        }
    }
}
