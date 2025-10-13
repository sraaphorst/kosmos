package org.vorpal.kosmos.algebra.structures

/**
 * A Semigroup with an identity element.
 */
interface Monoid<A> : Semigroup<A> {
    val identity: A
}
