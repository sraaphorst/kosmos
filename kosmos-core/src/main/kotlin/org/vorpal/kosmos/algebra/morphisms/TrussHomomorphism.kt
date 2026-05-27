package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Truss
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface TrussHomomorphism<A : Any, B : Any> : NonAssociativeTrussHomomorphism<A, B> {
    override val domain: Truss<A>
    override val codomain: Truss<B>

    infix fun <C : Any> andThen(other: TrussHomomorphism<B, C>): TrussHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: TrussHomomorphism<C, A>): TrussHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Truss<A>,
            codomain: Truss<B>,
            map: UnaryOp<A, B>
        ): TrussHomomorphism<A, B> = object : TrussHomomorphism<A, B> {
            override val domain: Truss<A> = domain
            override val codomain: Truss<B> = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Truss<A>,
            codomain: Truss<B>,
            map: (A) -> B
        ): TrussHomomorphism<A, B> = object : TrussHomomorphism<A, B> {
            override val domain: Truss<A> = domain
            override val codomain: Truss<B> = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}