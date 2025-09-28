package org.vorpal.kosmos.algebra.structures

/**
 * A Ring packs two operations: an operation that acts similar to:
 * * Multiplication
 * * Addition
 * with multiplication being distributive over addition. */
interface Ring<A> {
    val add: AbelianGroup<A>
    val mul: Monoid<A>
}

/**
 * Since CommutativeRings are special in the sense that they play so many roles in other algebraic structures,
 * they are included as an extension of Ring even though they add no inherent properties apart from being tagged
 * as being necessarily commutative.
 */
interface CommutativeRing<A> : Ring<A>
