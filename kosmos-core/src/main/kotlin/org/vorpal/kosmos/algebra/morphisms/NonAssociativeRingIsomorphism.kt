package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing

interface NonAssociativeRingIsomorphism<A : Any, B : Any> : NonAssociativeRingMonomorphism<A, B>, NonAssociativeRngIsomorphism<A, B> {
    override val backward: NonAssociativeRingHomomorphism<B, A>

    override val domain: NonAssociativeRing<A>
    override val codomain: NonAssociativeRing<B>

    override fun inverse(): NonAssociativeRingIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: NonAssociativeRingIsomorphism<B, C>): NonAssociativeRingIsomorphism<A, C> =
        of(
            forward = NonAssociativeRingHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: NonAssociativeRingIsomorphism<C, A>): NonAssociativeRingIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: NonAssociativeRingHomomorphism<A, B>,
            backward: NonAssociativeRingHomomorphism<B, A>
        ): NonAssociativeRingIsomorphism<A, B> = object : NonAssociativeRingIsomorphism<A, B> {
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
