package org.vorpal.kosmos.algebra.structures

/**
 * A [Semiring] does not attain the status of a [Ring], but has:
 * - A [CommutativeMonoid] for addition.
 * - A [Monoid] for multiplication.
 * with multiplication being distributive over addition.
 */
interface Semiring<A : Any>: Hemiring<A> {
    override val mul: Monoid<A>

    val one: A
        get() = mul.identity

    companion object {
        fun <A : Any> of(
            add: CommutativeMonoid<A>,
            mul: Monoid<A>,
        ): Semiring<A> = object : Semiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: Monoid<A> = mul
        }
    }
}
