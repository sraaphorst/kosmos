package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Rng

interface RngIsomorphism<A : Any, B : Any> : RngMonomorphism<A, B>, NonAssociativeRngIsomorphism<A, B> {
    override val backward: RngHomomorphism<B, A>

    override val domain: Rng<A>
    override val codomain: Rng<B>

    override fun inverse(): RngIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: RngIsomorphism<B, C>): RngIsomorphism<A, C> =
        of(
            forward = RngHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: RngIsomorphism<C, A>): RngIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: RngHomomorphism<A, B>,
            backward: RngHomomorphism<B, A>
        ): RngIsomorphism<A, B> = object : RngIsomorphism<A, B> {
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
