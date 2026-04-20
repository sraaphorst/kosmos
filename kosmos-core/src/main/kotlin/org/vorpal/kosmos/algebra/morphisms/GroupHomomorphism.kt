package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface GroupHomomorphism<A : Any, B : Any> : MonoidHomomorphism<A, B> {
    override val domain: Group<A>
    override val codomain: Group<B>

    infix fun <C : Any> andThen(other: GroupHomomorphism<B, C>): GroupHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: GroupHomomorphism<C, A>): GroupHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: UnaryOp<A, B>,
        ): GroupHomomorphism<A, B> = object : GroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: (A) -> B,
        ): GroupHomomorphism<A, B> = object : GroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
