package org.vorpal.kosmos.algebra.structures

/**
 * NonAssociativeAlgebra is an algebra with an:
 * - additive abelian group
 * - multiplicative nonassociative monoid
 */
interface NonAssociativeAlgebra<A : Any>: HasFromBigInt<A> {
    // Unnecessary, but specified for clarity.
    override val add: AbelianGroup<A>
    val mul: NonAssociativeMonoid<A>

    override val one: A
        get() = mul.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>
        ): NonAssociativeAlgebra<A> = object : NonAssociativeAlgebra<A> {
            override val add: AbelianGroup<A> = add
            override val mul: NonAssociativeMonoid<A> = mul
        }
    }
}
