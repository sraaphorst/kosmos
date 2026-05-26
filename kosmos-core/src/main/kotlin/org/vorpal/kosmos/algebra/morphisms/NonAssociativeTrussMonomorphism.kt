package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeTruss
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeTrussMonomorphism<A : Any, B : Any> : NonAssociativeTrussHomomorphism<A, B>, AlgebraicMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: NonAssociativeTrussMonomorphism<B, C>): NonAssociativeTrussMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: NonAssociativeTrussMonomorphism<C, A>): NonAssociativeTrussMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeTruss<A>,
            codomain: NonAssociativeTruss<B>,
            map: UnaryOp<A, B>
        ): NonAssociativeTrussMonomorphism<A, B> = object : NonAssociativeTrussMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeTruss<A>,
            codomain: NonAssociativeTruss<B>,
            map: (A) -> B
        ): NonAssociativeTrussMonomorphism<A, B> = object : NonAssociativeTrussMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}