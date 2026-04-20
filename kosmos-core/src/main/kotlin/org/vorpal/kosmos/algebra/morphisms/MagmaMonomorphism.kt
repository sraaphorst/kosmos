package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface MagmaMonomorphism<A : Any, B : Any> : MagmaHomomorphism<A, B>, AlgebraicMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: MagmaMonomorphism<B, C>): MagmaMonomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MagmaMonomorphism<C, A>): MagmaMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: UnaryOp<A, B>,
        ): MagmaMonomorphism<A, B> = object : MagmaMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: (A) -> B,
        ): MagmaMonomorphism<A, B> = object : MagmaMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
