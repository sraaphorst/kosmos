package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Heap
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface HeapHomomorphism<A : Any, B : Any> : AlgebraicHomomorphism<A, B> {
    val domain: Heap<A>
    val codomain: Heap<B>

    infix fun <C : Any> andThen(other: HeapHomomorphism<B, C>): HeapHomomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C : Any> compose(other: HeapHomomorphism<C, A>): HeapHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Heap<A>,
            codomain: Heap<B>,
            map: UnaryOp<A, B>
        ): HeapHomomorphism<A, B> = object : HeapHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Heap<A>,
            codomain: Heap<B>,
            map: (A) -> B
        ): HeapHomomorphism<A, B> = of(domain, codomain, UnaryOp(Symbols.PHI, map))
    }
}
