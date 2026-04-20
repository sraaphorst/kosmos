package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semiring

interface SemiringIsomorphism<A : Any, B : Any> : SemiringMonomorphism<A, B>, AlgebraicIsomorphism<A, B> {
    override val backward: SemiringHomomorphism<B, A>

    override val domain: Semiring<A>
    override val codomain: Semiring<B>

    override fun inverse(): SemiringIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: SemiringIsomorphism<B, C>): SemiringIsomorphism<A, C> =
        of(
            forward = SemiringHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: SemiringIsomorphism<C, A>): SemiringIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: SemiringHomomorphism<A, B>,
            backward: SemiringHomomorphism<B, A>
        ): SemiringIsomorphism<A, B> = object : SemiringIsomorphism<A, B> {
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
