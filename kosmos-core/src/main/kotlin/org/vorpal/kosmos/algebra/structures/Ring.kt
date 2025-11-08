package org.vorpal.kosmos.algebra.structures

/**
 * A Ring packs two operations: an operation that acts similar to:
 * * Multiplication
 * * Addition
 * with multiplication being distributive over addition. */
interface Ring<A: Any> {
    val add: AbelianGroup<A>
    val mul: Monoid<A>

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
 * Since CommutativeRings are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Ring even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface CommutativeRing<A: Any> : Ring<A> {
    companion object {
        fun <A: Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
        ): CommutativeRing<A> = object : CommutativeRing<A> {
            override val add: AbelianGroup<A> = add
            override val mul: Monoid<A> = mul
        }
    }
}
