package org.vorpal.kosmos.algebra.structures


/**
 * A [CommutativeSemiring] does not achieve the status of a [Ring], but
 * the multiplicative monoid is a [CommutativeMonoid].
 * with multiplication being distributive over addition.
 */
interface CommutativeSemiring<A: Any>: Semiring<A> {
    override val mul: CommutativeMonoid<A>

    companion object {
        fun <A: Any> of(
            add: CommutativeMonoid<A>,
            mul: CommutativeMonoid<A>
        ): CommutativeSemiring<A> = object : CommutativeSemiring<A> {
            override val add: CommutativeMonoid<A> = add
            override val mul: CommutativeMonoid<A> = mul
        }
    }
}
