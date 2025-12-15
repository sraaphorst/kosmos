package org.vorpal.kosmos.algebra.structures

/**
 * A [Rng] with commutative multiplication.
 */
interface CommutativeRng<A : Any>: Rng<A> {
    override val add: AbelianGroup<A>
    override val mul: CommutativeSemigroup<A>

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeSemigroup<A>
        ): CommutativeRng<A> = object : CommutativeRng<A> {
            override val add = add
            override val mul = mul
        }
    }
}
