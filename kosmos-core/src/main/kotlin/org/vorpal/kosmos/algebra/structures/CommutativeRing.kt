package org.vorpal.kosmos.algebra.structures


/**
 * Since CommutativeRings are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Ring even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface CommutativeRing<A: Any> : Ring<A> {
    companion object {
        fun <A: Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
        ): CommutativeRing<A> = object : CommutativeRing<A> {
            override val add: AbelianGroup<A> = add
            override val mul: CommutativeMonoid<A> = mul
        }
    }
}
