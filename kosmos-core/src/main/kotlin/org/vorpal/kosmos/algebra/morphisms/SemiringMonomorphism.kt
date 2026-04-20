package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.categories.Monomorphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface SemiringMonomorphism<A : Any, B : Any> : SemiringHomomorphism<A, B>, Monomorphism<A, B> {
    override val domain: Semiring<A>
    override val codomain: Semiring<B>
    override val map: UnaryOp<A, B>

    infix fun <C : Any> andThen(other: SemiringMonomorphism<B, C>): SemiringMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: SemiringMonomorphism<C, A>): SemiringMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Semiring<A>,
            codomain: Semiring<B>,
            map: UnaryOp<A, B>
        ): SemiringMonomorphism<A, B> = object : SemiringMonomorphism<A, B> {
            override val domain: Semiring<A> = domain
            override val codomain: Semiring<B> = codomain
            override val map: UnaryOp<A, B> = map
        }

        fun <A : Any, B : Any> of(
            domain: Semiring<A>,
            codomain: Semiring<B>,
            map: (A) -> B
        ): SemiringMonomorphism<A, B> = object : SemiringMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
