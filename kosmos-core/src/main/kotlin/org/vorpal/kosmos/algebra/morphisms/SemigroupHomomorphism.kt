package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface SemigroupHomomorphism<A : Any, B : Any>: MagmaHomomorphism<A, B> {
    override val domain: Semigroup<A>
    override val codomain: Semigroup<B>

    infix fun <C : Any> andThen(other: SemigroupHomomorphism<B, C>): SemigroupHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: SemigroupHomomorphism<C, A>): SemigroupHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Semigroup<A>,
            codomain: Semigroup<B>,
            map: UnaryOp<A, B>
        ): SemigroupHomomorphism<A, B> = object : SemigroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Semigroup<A>,
            codomain: Semigroup<B>,
            map: (A) -> B,
        ): SemigroupHomomorphism<A, B> = object : SemigroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
