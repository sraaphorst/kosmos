package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Heap

interface HeapIsomorphism<A : Any, B : Any> : HeapHomomorphism<A, B>, AlgebraicIsomorphism<A, B> {
    override val backward: HeapHomomorphism<B, A>

    override val domain: Heap<A>
    override val codomain: Heap<B>

    override fun inverse(): HeapIsomorphism<B, A> =
        of(backward, this)

    infix fun <C : Any> andThen(other: HeapIsomorphism<B, C>): HeapIsomorphism<A, C> =
        of(
            forward = HeapHomomorphism.of(
                domain = domain,
                codomain = other.codomain,
                map = map andThen other.map
            ),
            backward = other.backward andThen backward
        )

    infix fun <C : Any> compose(other: HeapIsomorphism<C, A>): HeapIsomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            forward: HeapHomomorphism<A, B>,
            backward: HeapHomomorphism<B, A>
        ): HeapIsomorphism<A, B> = object : HeapIsomorphism<A, B> {
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
