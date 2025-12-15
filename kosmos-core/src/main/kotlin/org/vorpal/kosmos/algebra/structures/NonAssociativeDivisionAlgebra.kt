package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

interface NonAssociativeDivisionAlgebra<A : Any> : InvolutiveAlgebra<A>, HasReciprocal<A> {
    override val zero: A
        get() = add.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            reciprocal: Endo<A>,
            conj: Endo<A>
        ): NonAssociativeDivisionAlgebra<A> = object : NonAssociativeDivisionAlgebra<A> {
            override val zero: A = add.identity
            override val add = add
            override val mul = mul
            override val reciprocal = reciprocal
            override val conj = conj
        }
    }
}
