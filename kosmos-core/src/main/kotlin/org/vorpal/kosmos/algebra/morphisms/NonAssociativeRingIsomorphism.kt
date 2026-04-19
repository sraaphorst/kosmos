package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.categories.Isomorphism

/**
 * An isomorphism in the category of nonassociative rings.
 *
 * This is both:
 * - a categorical [Isomorphism] (as plain functions on carriers); and
 * - a pair of mutually inverse [NonAssociativeRingHomomorphism]s with domain/codomain witnesses.
 */
interface NonAssociativeRingIsomorphism<A : Any, B : Any> : Isomorphism<A, B> {
    override val forward: NonAssociativeRingHomomorphism<A, B>
    override val backward: NonAssociativeRingHomomorphism<B, A>

    val domain: NonAssociativeRing<A>
        get() = forward.domain

    val codomain: NonAssociativeRing<B>
        get() = forward.codomain

    override fun inverse(): NonAssociativeRingIsomorphism<B, A> =
        of(backward, forward)

    companion object {
        fun <A : Any, B : Any> of(
            forward: NonAssociativeRingHomomorphism<A, B>,
            backward: NonAssociativeRingHomomorphism<B, A>,
        ): NonAssociativeRingIsomorphism<A, B> = object : NonAssociativeRingIsomorphism<A, B> {
            init {
                require(forward.codomain === backward.domain) {
                    "codomain/domain mismatch: forward.codomain != backward.domain"
                }
                require(forward.domain === backward.codomain) {
                    "domain/codomain mismatch: forward.domain != backward.codomain"
                }
            }
            override val forward = forward
            override val backward = backward
        }
    }
}
