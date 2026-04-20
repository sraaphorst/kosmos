package org.vorpal.kosmos.algebra.structures

/**
 * A [Rng] is a ring without a multiplicative identity.
 *
 * Equivalently: a hemiring whose additive commutative monoid is an abelian group.
 */
interface Rng<A : Any> : Hemiring<A>, NonAssociativeRng<A> {
    override val add: AbelianGroup<A>
    override val mul: Semigroup<A>

    override val zero: A
        get() = add.identity

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: Semigroup<A>
        ): Rng<A> = object : Rng<A> {
            override val add = add
            override val mul = mul
        }
    }
}
