package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Truss

interface TrussIsomorphism<A : Any, B : Any> : TrussMonomorphism<A, B>, NonAssociativeTrussIsomorphism<A, B> {
    override val backward: TrussHomomorphism<B, A>

    override val domain: Truss<A>
    override val codomain: Truss<B>

    override fun inverse(): TrussIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: TrussIsomorphism<B, C>): TrussIsomorphism<A, C> =
        of(
            forward = TrussHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: TrussIsomorphism<C, A>): TrussIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: TrussHomomorphism<A, B>,
            backward: TrussHomomorphism<B, A>
        ): TrussIsomorphism<A, B> = object : TrussIsomorphism<A, B> {
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