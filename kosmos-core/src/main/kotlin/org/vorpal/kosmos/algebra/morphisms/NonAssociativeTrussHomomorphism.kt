package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeTruss
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeTrussHomomorphism<A : Any, B : Any> : AlgebraicHomomorphism<A, B> {
    val domain: NonAssociativeTruss<A>
    val codomain: NonAssociativeTruss<B>

    infix fun <C : Any> andThen(other: NonAssociativeTrussHomomorphism<B, C>): NonAssociativeTrussHomomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C : Any> compose(other: NonAssociativeTrussHomomorphism<C, A>): NonAssociativeTrussHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeTruss<A>,
            codomain: NonAssociativeTruss<B>,
            map: UnaryOp<A, B>
        ): NonAssociativeTrussHomomorphism<A, B> = object : NonAssociativeTrussHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: NonAssociativeTruss<A>,
            codomain: NonAssociativeTruss<B>,
            map: (A) -> B
        ): NonAssociativeTrussHomomorphism<A, B> = object : NonAssociativeTrussHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}