package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface MonoidMonomorphism<A : Any, B : Any> : MonoidHomomorphism<A, B>, SemigroupMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: MonoidMonomorphism<B, C>): MonoidMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MonoidMonomorphism<C, A>): MonoidMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: UnaryOp<A, B>,
        ): MonoidMonomorphism<A, B> = object : MonoidMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: (A) -> B,
        ): MonoidMonomorphism<A, B> = object : MonoidMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
