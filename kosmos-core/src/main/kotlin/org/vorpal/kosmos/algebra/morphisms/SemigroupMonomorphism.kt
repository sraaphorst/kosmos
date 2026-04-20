package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface SemigroupMonomorphism<A : Any, B : Any> : SemigroupHomomorphism<A, B>, MagmaMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: SemigroupMonomorphism<B, C>): SemigroupMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: SemigroupMonomorphism<C, A>): SemigroupMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Semigroup<A>,
            codomain: Semigroup<B>,
            map: UnaryOp<A, B>,
        ): SemigroupMonomorphism<A, B> = object : SemigroupMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Semigroup<A>,
            codomain: Semigroup<B>,
            map: (A) -> B,
        ): SemigroupMonomorphism<A, B> = object : SemigroupMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
