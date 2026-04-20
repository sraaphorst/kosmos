package org.vorpal.kosmos.algebra.structures

interface NonAssociativeRing<A : Any>: NonAssociativeRng<A>, HasFromBigInt<A> {
    override val mul: NonAssociativeMonoid<A>

    override val one: A
        get() = mul.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>
        ): NonAssociativeRing<A> = object : NonAssociativeRing<A> {
            override val add: AbelianGroup<A> = add
            override val mul: NonAssociativeMonoid<A> = mul
        }
    }
}
