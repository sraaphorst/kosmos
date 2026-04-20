package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface GroupMonomorphism<A : Any, B : Any> : GroupHomomorphism<A, B>, MonoidMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: GroupMonomorphism<B, C>): GroupMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: GroupMonomorphism<C, A>): GroupMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: UnaryOp<A, B>,
        ): GroupMonomorphism<A, B> = object : GroupMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: (A) -> B,
        ): GroupMonomorphism<A, B> = object : GroupMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
