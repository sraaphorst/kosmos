package org.vorpal.kosmos.algebra.structures

interface NonAssociativeRng<A : Any> {
    val add: AbelianGroup<A>
    val mul: NonAssociativeSemigroup<A>

    val zero: A
        get() = add.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeSemigroup<A>
        ): NonAssociativeRng<A> = object : NonAssociativeRng<A> {
            override val add: AbelianGroup<A> = add
            override val mul: NonAssociativeSemigroup<A> = mul
        }
    }
}
