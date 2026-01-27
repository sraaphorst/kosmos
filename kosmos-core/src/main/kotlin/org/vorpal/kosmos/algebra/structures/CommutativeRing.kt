package org.vorpal.kosmos.algebra.structures

/**
 * A [Ring] where the multiplicative operator is commutative.
 */
interface CommutativeRing<A : Any> :
    Ring<A>,
    CommutativeRng<A>,
    CommutativeSemiring<A> {

    override val mul: CommutativeMonoid<A>

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
        ): CommutativeRing<A> = object : CommutativeRing<A> {
            override val add = add
            override val mul = mul
        }
    }
}
