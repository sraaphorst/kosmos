package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group

interface GroupIsomorphism<A : Any, B : Any> : GroupHomomorphism<A, B>, MonoidIsomorphism<A, B> {
    override val backward: GroupHomomorphism<B, A>

    override val domain: Group<A>
    override val codomain: Group<B>

    override fun inverse(): GroupIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: GroupIsomorphism<B, C>): GroupIsomorphism<A, C> =
        of(
            forward = GroupHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: GroupIsomorphism<C, A>): GroupIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: GroupHomomorphism<A, B>,
            backward: GroupHomomorphism<B, A>
        ): GroupIsomorphism<A, B> = object : GroupIsomorphism<A, B> {
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
