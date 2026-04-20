package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRng
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRngHomomorphism<A: Any, B: Any> : AlgebraicHomomorphism<A, B> {
    val domain: NonAssociativeRng<A>
    val codomain: NonAssociativeRng<B>

    infix fun <C : Any> andThen(other: NonAssociativeRngHomomorphism<B, C>): NonAssociativeRngHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: NonAssociativeRngHomomorphism<C, A>): NonAssociativeRngHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeRng<A>,
            codomain: NonAssociativeRng<B>,
            map: UnaryOp<A, B>
        ): NonAssociativeRngHomomorphism<A, B> = object : NonAssociativeRngHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeRng<A>,
            codomain: NonAssociativeRng<B>,
            map: (A) -> B,
        ): NonAssociativeRngHomomorphism<A, B> = object : NonAssociativeRngHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
