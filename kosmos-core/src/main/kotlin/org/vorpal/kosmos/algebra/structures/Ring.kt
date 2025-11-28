package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger



/**
 * A Ring packs two operations: an operation that acts similar to:
 * - An [AbelianGroup] for addition.
 * - A [Monoid] for multiplication.
 * with multiplication being distributive over addition. */
interface Ring<A: Any>: NonAssociativeAlgebra<A>, Semiring<A> {
    override val add: AbelianGroup<A>

    companion object {
        fun <A: Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
        ): Ring<A> = object : Ring<A> {
            override val add: AbelianGroup<A> = add
            override val mul: Monoid<A> = mul
        }
    }
}

/**
 * Convenience function to get the negation of the multiplicative identity.
 */
val <A: Any> Ring<A>.negOne: A
    get() = add.inverse(mul.identity)

