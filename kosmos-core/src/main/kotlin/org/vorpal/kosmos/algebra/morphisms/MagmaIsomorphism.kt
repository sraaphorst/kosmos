package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Magma

interface MagmaIsomorphism<A : Any, B : Any> : MagmaMonomorphism<A, B>, AlgebraicIsomorphism<A, B> {
    override val backward: MagmaHomomorphism<B, A>

    override val domain: Magma<A>
    override val codomain: Magma<B>

    override fun inverse(): MagmaIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: MagmaIsomorphism<B, C>): MagmaIsomorphism<A, C> =
        of(
            forward = MagmaHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: MagmaIsomorphism<C, A>): MagmaIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: MagmaHomomorphism<A, B>,
            backward: MagmaHomomorphism<B, A>
        ): MagmaIsomorphism<A, B> = object : MagmaIsomorphism<A, B> {
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
