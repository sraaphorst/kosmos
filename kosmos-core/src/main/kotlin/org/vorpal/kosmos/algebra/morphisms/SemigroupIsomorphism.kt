package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semigroup

interface SemigroupIsomorphism<A : Any, B : Any> : SemigroupMonomorphism<A, B>, MagmaIsomorphism<A, B> {
    override val backward: SemigroupHomomorphism<B, A>

    override val domain: Semigroup<A>
    override val codomain: Semigroup<B>

    override fun inverse(): SemigroupIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: SemigroupIsomorphism<B, C>): SemigroupIsomorphism<A, C> =
        of(
            forward = SemigroupHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: SemigroupIsomorphism<C, A>): SemigroupIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: SemigroupHomomorphism<A, B>,
            backward: SemigroupHomomorphism<B, A>
        ): SemigroupIsomorphism<A, B> = object : SemigroupIsomorphism<A, B> {
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
