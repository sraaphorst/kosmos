package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.categories.Isomorphism

/**
 * An isomorphism in the category of rings.
 *
 * This is both:
 * - a categorical [Isomorphism] (as plain functions on carriers), and
 * - a pair of mutually inverse [RingHomomorphism]s with domain/codomain witnesses.
 */
interface RingIsomorphism<A : Any, B : Any> : Isomorphism<A, B> {
    override val forward: RingHomomorphism<A, B>
    override val backward: RingHomomorphism<B, A>

    val domain: Ring<A>
        get() = forward.domain

    val codomain: Ring<B>
        get() = forward.codomain

    override fun inverse(): RingIsomorphism<B, A> =
        of(backward, forward)

    companion object {
        fun <A : Any, B : Any> of(
            forward: RingHomomorphism<A, B>,
            backward: RingHomomorphism<B, A>,
        ): RingIsomorphism<A, B> = object : RingIsomorphism<A, B> {
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
