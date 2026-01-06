package org.vorpal.kosmos.algebra.structures

/**
 * NonAssociativeAlgebra is an algebra with an:
 * - additive abelian group
 * - multiplicative nonassociative monoid
 */
interface NonAssociativeRing<A : Any>: HasFromBigInt<A> {
    // Unnecessary, but specified for clarity.
    override val add: AbelianGroup<A>
    val mul: NonAssociativeMonoid<A>

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
