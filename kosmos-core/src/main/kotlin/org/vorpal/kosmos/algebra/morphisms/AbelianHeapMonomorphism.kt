package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface AbelianHeapMonomorphism<A : Any, B : Any> : AbelianHeapHomomorphism<A, B>, HeapMonomorphism<A, B> {
    infix fun <C : Any> andThen(other: AbelianHeapMonomorphism<B, C>): AbelianHeapMonomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C : Any> compose(other: AbelianHeapMonomorphism<C, A>): AbelianHeapMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: AbelianHeap<A>,
            codomain: AbelianHeap<B>,
            map: UnaryOp<A, B>
        ): AbelianHeapMonomorphism<A, B> = object : AbelianHeapMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: AbelianHeap<A>,
            codomain: AbelianHeap<B>,
            map: (A) -> B
        ): AbelianHeapMonomorphism<A, B> = object : AbelianHeapMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
