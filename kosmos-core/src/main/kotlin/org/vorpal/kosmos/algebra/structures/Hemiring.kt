package org.vorpal.kosmos.algebra.structures


/**
 * A [Hemiring] does not attain the status of a [Ring], but has:
 * - A [CommutativeMonoid] for addition.
 * - A [Semigroup] for multiplication.
 * with multiplication being distributive over addition.
 */
interface Hemiring<A: Any> {
    val add: CommutativeMonoid<A>
    val mul: Semigroup<A>

    companion object {
        fun <A: Any> of(
            add: CommutativeMonoid<A>,
            mul: Semigroup<A>,
        ): Hemiring<A> = object : Hemiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: Semigroup<A> = mul
        }
    }
}
