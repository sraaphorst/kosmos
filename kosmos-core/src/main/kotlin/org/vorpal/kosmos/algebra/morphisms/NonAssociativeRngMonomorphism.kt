package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRng
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRngMonomorphism<A : Any, B : Any> : NonAssociativeRngHomomorphism<A, B>, AlgebraicMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: NonAssociativeRngMonomorphism<B, C>): NonAssociativeRngMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: NonAssociativeRngMonomorphism<C, A>): NonAssociativeRngMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeRng<A>,
            codomain: NonAssociativeRng<B>,
            map: UnaryOp<A, B>,
        ): NonAssociativeRngMonomorphism<A, B> = object : NonAssociativeRngMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeRng<A>,
            codomain: NonAssociativeRng<B>,
            map: (A) -> B,
        ): NonAssociativeRngMonomorphism<A, B> = object : NonAssociativeRngMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
