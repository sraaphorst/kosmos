package org.vorpal.kosmos.algebra.structures

/**
 * A Ring packs two operations: an operation that acts similar to:
 * - An [AbelianGroup] for addition.
 * - A [Monoid] for multiplication.
 * with multiplication being distributive over addition. */
interface Ring<A : Any>: Semiring<A>, Rng<A>, HasFromBigInt<A> {
    // Not needed, but specified for clarity.
    override val add: AbelianGroup<A>
    override val mul: Monoid<A>

    override val one: A
        get() = mul.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
        ): Ring<A> = object : Ring<A> {
            override val add = add
            override val mul = mul
        }
    }
}
