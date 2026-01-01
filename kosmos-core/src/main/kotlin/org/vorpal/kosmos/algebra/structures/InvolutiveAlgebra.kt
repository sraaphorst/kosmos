package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

interface InvolutiveAlgebra<A : Any> : NonAssociativeAlgebra<A> {
    /**
     * The involution operator of the algebra, `x ↦ x*`.
     */
    val conj: Endo<A>

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            conj: Endo<A>
        ): InvolutiveAlgebra<A> = object : InvolutiveAlgebra<A> {
            override val add = add
            override val mul = mul
            override val conj = conj
        }
    }
}
