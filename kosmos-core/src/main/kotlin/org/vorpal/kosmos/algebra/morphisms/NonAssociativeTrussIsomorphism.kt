package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeTruss

interface NonAssociativeTrussIsomorphism<A : Any, B : Any> : NonAssociativeTrussMonomorphism<A, B>, AlgebraicIsomorphism<A, B> {
    override val backward: NonAssociativeTrussHomomorphism<B, A>

    override val domain: NonAssociativeTruss<A>
    override val codomain: NonAssociativeTruss<B>

    override fun inverse(): NonAssociativeTrussIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: NonAssociativeTrussIsomorphism<B, C>): NonAssociativeTrussIsomorphism<A, C> =
        of(
            forward = NonAssociativeTrussHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: NonAssociativeTrussIsomorphism<C, A>): NonAssociativeTrussIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: NonAssociativeTrussHomomorphism<A, B>,
            backward: NonAssociativeTrussHomomorphism<B, A>
        ): NonAssociativeTrussIsomorphism<A, B> = object : NonAssociativeTrussIsomorphism<A, B> {
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
