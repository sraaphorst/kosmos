package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface SemiringHomomorphism<A : Any, B : Any> : AlgebraicHomomorphism<A, B> {
    val domain: Semiring<A>
    val codomain: Semiring<B>

    infix fun <C : Any> andThen(other: SemiringHomomorphism<B, C>): SemiringHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: SemiringHomomorphism<C, A>): SemiringHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Semiring<A>,
            codomain: Semiring<B>,
            map: UnaryOp<A, B>
        ): SemiringHomomorphism<A, B> = object : SemiringHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Semiring<A>,
            codomain: Semiring<B>,
            map: (A) -> B
        ): SemiringHomomorphism<A, B> = object : SemiringHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
