package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid

interface MonoidIsomorphism<A : Any, B : Any> : MonoidMonomorphism<A, B>, SemigroupIsomorphism<A, B> {
    override val backward: MonoidHomomorphism<B, A>

    override val domain: Monoid<A>
    override val codomain: Monoid<B>

    override fun inverse(): MonoidIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: MonoidIsomorphism<B, C>): MonoidIsomorphism<A, C> =
        of(
            forward = MonoidHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: MonoidIsomorphism<C, A>): MonoidIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: MonoidHomomorphism<A, B>,
            backward: MonoidHomomorphism<B, A>
        ): MonoidIsomorphism<A, B> = object : MonoidIsomorphism<A, B> {
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
