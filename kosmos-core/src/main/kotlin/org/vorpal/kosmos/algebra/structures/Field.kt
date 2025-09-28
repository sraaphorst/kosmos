package org.vorpal.kosmos.algebra.structures

/**
 * A Field is a commutative Ring where the multiplicative operator has inverses for
 * all elements except for the additive identity.
 */
interface Field<A> : CommutativeRing<A> {
    override val mul: AbelianGroup<A>
}
