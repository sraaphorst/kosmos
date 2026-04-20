package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface RngHomomorphism<A : Any, B : Any> : NonAssociativeRngHomomorphism<A, B> {
    override val domain: Rng<A>
    override val codomain: Rng<B>

    infix fun <C : Any> andThen(other: RngHomomorphism<B, C>): RngHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: RngHomomorphism<C, A>): RngHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Rng<A>,
            codomain: Rng<B>,
            map: UnaryOp<A, B>
        ): RngHomomorphism<A, B> = object : RngHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Rng<A>,
            codomain: Rng<B>,
            map: (A) -> B,
        ): RngHomomorphism<A, B> = object : RngHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
