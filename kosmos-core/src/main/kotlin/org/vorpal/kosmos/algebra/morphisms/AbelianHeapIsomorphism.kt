package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.AbelianHeap

interface AbelianHeapIsomorphism<A : Any, B : Any> : AbelianHeapHomomorphism<A, B>, HeapIsomorphism<A, B> {
    override val backward: AbelianHeapHomomorphism<B, A>

    override val domain: AbelianHeap<A>
    override val codomain: AbelianHeap<B>

    override fun inverse(): AbelianHeapIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: AbelianHeapIsomorphism<B, C>): AbelianHeapIsomorphism<A, C> =
        of(
            forward = AbelianHeapHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: AbelianHeapIsomorphism<C, A>): AbelianHeapIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: AbelianHeapHomomorphism<A, B>,
            backward: AbelianHeapHomomorphism<B, A>
        ): AbelianHeapIsomorphism<A, B> = object : AbelianHeapIsomorphism<A, B> {
            init {
                require(forward.codomain === backward.domain) { "codomain / domain mismatch" }
                require(forward.domain === backward.codomain) { "domain / codomain mismatch" }
            }
            override val domain = forward.domain
            override val codomain = forward.codomain
            override val map = forward.map
            override val backward = backward
        }
    }
}