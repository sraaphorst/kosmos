package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface RngMonomorphism<A : Any, B : Any> : RngHomomorphism<A, B>, NonAssociativeRngMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: RngMonomorphism<B, C>): RngMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: RngMonomorphism<C, A>): RngMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Rng<A>,
            codomain: Rng<B>,
            map: UnaryOp<A, B>,
        ): RngMonomorphism<A, B> = object : RngMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Rng<A>,
            codomain: Rng<B>,
            map: (A) -> B,
        ): RngMonomorphism<A, B> = object : RngMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
