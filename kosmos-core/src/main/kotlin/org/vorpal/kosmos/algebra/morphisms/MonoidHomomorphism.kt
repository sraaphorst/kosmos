package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface MonoidHomomorphism<A : Any, B : Any> : SemigroupHomomorphism<A, B> {
    override val domain: Monoid<A>
    override val codomain: Monoid<B>

    infix fun <C : Any> andThen(other: MonoidHomomorphism<B, C>): MonoidHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MonoidHomomorphism<C, A>): MonoidHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: UnaryOp<A, B>,
        ): MonoidHomomorphism<A, B> = object : MonoidHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: (A) -> B,
        ): MonoidHomomorphism<A, B> = object : MonoidHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
