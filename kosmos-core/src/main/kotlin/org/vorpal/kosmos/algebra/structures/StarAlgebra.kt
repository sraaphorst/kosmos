package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction

/**
 * We get the scalar ring, `CommutativeRing<R>` from the `Algebra<R, A>`.
 * We get the ring on `A` and `conj: Endo<A>` from `InvolutiveRing<A>`.
 */
interface StarAlgebra<R : Any, A : Any> : Algebra<R, A>, InvolutiveRing<A> {
    override val one: A

    companion object {
        fun <R : Any, A : Any> of(
            scalars: CommutativeRing<R>,
            involutiveRing: InvolutiveRing<A>,
            leftAction: LeftAction<R, A>
        ): StarAlgebra<R, A> = object : StarAlgebra<R, A> {
            override val scalars = scalars
            override val group = involutiveRing.add
            override val add = group
            override val mul = involutiveRing.mul
            override val conj = involutiveRing.conj
            override val leftAction = leftAction
            override val one = involutiveRing.one
        }
    }
}
