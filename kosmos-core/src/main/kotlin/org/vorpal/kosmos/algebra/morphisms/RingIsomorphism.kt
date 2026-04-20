package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Ring

interface RingIsomorphism<A : Any, B : Any> :
    RingMonomorphism<A, B>, NonAssociativeRingIsomorphism<A, B>, RngIsomorphism<A, B> {
    override val backward: RingHomomorphism<B, A>

    override val domain: Ring<A>
    override val codomain: Ring<B>

    override fun inverse(): RingIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: RingIsomorphism<B, C>): RingIsomorphism<A, C> =
        of(
            forward = RingHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: RingIsomorphism<C, A>): RingIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: RingHomomorphism<A, B>,
            backward: RingHomomorphism<B, A>
        ): RingIsomorphism<A, B> = object : RingIsomorphism<A, B> {
            init {
                require(forward.codomain === backward.domain) { "codomain / domain mismatch" }
                require(forward.domain === backward.codomain) { "domain / codomain mismatch" }
            }
            override val domain = forward.domain
            override val codomain = forward.codomain
            override val map = forward.map
            override val backward = backward
        }
    }
}
