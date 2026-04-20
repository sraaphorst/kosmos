package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRingMonomorphism<A : Any, B : Any> : NonAssociativeRingHomomorphism<A, B>, AlgebraicMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: NonAssociativeRingMonomorphism<B, C>): NonAssociativeRingMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: NonAssociativeRingMonomorphism<C, A>): NonAssociativeRingMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: UnaryOp<A, B>,
        ): NonAssociativeRingMonomorphism<A, B> = object : NonAssociativeRingMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: (A) -> B,
        ): NonAssociativeRingMonomorphism<A, B> = object : NonAssociativeRingMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
