package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction

/**
 * A non-associative algebra is a non-associative ring acted on by a commutative ring of scalars.
 */
interface NonAssociativeAlgebra<R : Any, A : Any> :
    RModule<R, A>,
    NonAssociativeRing<A> {
    override val scalars: CommutativeRing<R>

    companion object {
        fun <R : Any, A : Any> of(
            scalars: CommutativeRing<R>,
            algebraRing: NonAssociativeRing<A>,
            leftAction: LeftAction<R, A>
        ): NonAssociativeAlgebra<R, A> = object : NonAssociativeAlgebra<R, A> {
            override val scalars = scalars
            override val add = algebraRing.add
            override val mul = algebraRing.mul
            override val leftAction = leftAction
        }
    }
}
