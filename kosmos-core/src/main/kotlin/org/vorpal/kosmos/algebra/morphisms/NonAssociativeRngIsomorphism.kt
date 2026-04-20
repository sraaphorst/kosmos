package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRng

interface NonAssociativeRngIsomorphism<A : Any, B : Any> : NonAssociativeRngMonomorphism<A, B>, AlgebraicIsomorphism<A, B> {
    override val backward: NonAssociativeRngHomomorphism<B, A>

    override val domain: NonAssociativeRng<A>
    override val codomain: NonAssociativeRng<B>

    override fun inverse(): NonAssociativeRngIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: NonAssociativeRngIsomorphism<B, C>): NonAssociativeRngIsomorphism<A, C> =
        of(
            forward = NonAssociativeRngHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: NonAssociativeRngIsomorphism<C, A>): NonAssociativeRngIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: NonAssociativeRngHomomorphism<A, B>,
            backward: NonAssociativeRngHomomorphism<B, A>
        ): NonAssociativeRngIsomorphism<A, B> = object : NonAssociativeRngIsomorphism<A, B> {
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
