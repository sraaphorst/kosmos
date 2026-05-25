package org.vorpal.kosmos.algebra.structures

interface NonAssociativeRng<A : Any> {
    val add: AbelianGroup<A>
    val mul: Magma<A>

    val zero: A
        get() = add.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: Magma<A>
        ): NonAssociativeRng<A> = object : NonAssociativeRng<A> {
            override val add = add
            override val mul = mul
        }
    }
}
