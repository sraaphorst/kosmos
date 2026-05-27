package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Truss
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface TrussMonomorphism<A : Any, B : Any>: TrussHomomorphism<A, B>, NonAssociativeTrussMonomorphism<A, B> {

    infix fun <C : Any> andThen(other: TrussMonomorphism<B, C>): TrussMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: TrussMonomorphism<C, A>): TrussMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Truss<A>,
            codomain: Truss<B>,
            map: UnaryOp<A, B>
        ): TrussMonomorphism<A, B> = object : TrussMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Truss<A>,
            codomain: Truss<B>,
            map: (A) -> B
        ): TrussMonomorphism<A, B> = object : TrussMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}