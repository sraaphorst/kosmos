package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction

interface NonAssociativeStarAlgebra<R : Any, A : Any> :
    NonAssociativeAlgebra<R, A>,
    HasConj<A>
{
    companion object {
        fun <R : Any, A : Any> of(
            scalars: CommutativeRing<R>,
            involutiveRing: NonAssociativeInvolutiveRing<A>,
            leftAction: LeftAction<R, A>
        ): NonAssociativeStarAlgebra<R, A> = object : NonAssociativeStarAlgebra<R, A> {
            override val scalars = scalars
            override val add = involutiveRing.add
            override val mul = involutiveRing.mul
            override val conj = involutiveRing.conj
            override val leftAction = leftAction
            override val one = involutiveRing.one
        }
    }
}
