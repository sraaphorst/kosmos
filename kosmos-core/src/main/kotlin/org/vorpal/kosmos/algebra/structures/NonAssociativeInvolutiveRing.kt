package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

interface NonAssociativeInvolutiveRing<A : Any>: NonAssociativeRing<A>, HasConj<A> {
    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            conj: Endo<A>
        ): NonAssociativeInvolutiveRing<A> = object : NonAssociativeInvolutiveRing<A> {
            override val add = add
            override val mul = mul
            override val conj = conj
        }
    }
}
