package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRingHomomorphism<A : Any, B : Any> : NonAssociativeRngHomomorphism<A, B> {
    override val domain: NonAssociativeRing<A>
    override val codomain: NonAssociativeRing<B>

    infix fun <C : Any> andThen(other: NonAssociativeRingHomomorphism<B, C>): NonAssociativeRingHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: NonAssociativeRingHomomorphism<C, A>): NonAssociativeRingHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: UnaryOp<A, B>
        ): NonAssociativeRingHomomorphism<A, B> = object : NonAssociativeRingHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: (A) -> B,
        ): NonAssociativeRingHomomorphism<A, B> = object : NonAssociativeRingHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
