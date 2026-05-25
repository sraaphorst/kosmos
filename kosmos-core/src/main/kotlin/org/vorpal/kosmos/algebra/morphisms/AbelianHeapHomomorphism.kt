package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface AbelianHeapHomomorphism<A : Any, B : Any> : HeapHomomorphism<A, B> {
    override val domain: AbelianHeap<A>
    override val codomain: AbelianHeap<B>

    infix fun <C : Any> andThen(other: AbelianHeapHomomorphism<B, C>): AbelianHeapHomomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C : Any> compose(other: AbelianHeapHomomorphism<C, A>): AbelianHeapHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: AbelianHeap<A>,
            codomain: AbelianHeap<B>,
            map: UnaryOp<A, B>
        ): AbelianHeapHomomorphism<A, B> = object : AbelianHeapHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: AbelianHeap<A>,
            codomain: AbelianHeap<B>,
            map: (A) -> B
        ): AbelianHeapHomomorphism<A, B> = of(domain, codomain, UnaryOp(Symbols.PHI, map))
    }
}
