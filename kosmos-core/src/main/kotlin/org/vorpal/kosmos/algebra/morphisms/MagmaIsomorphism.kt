package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.categories.Isomorphism

interface MagmaIsomorphism<A : Any, B : Any> : Isomorphism<A, B> {
    override val forward: MagmaHomomorphism<A, B>
    override val backward: MagmaHomomorphism<B, A>

    val domain: Magma<A>
        get() = forward.domain
    val codomain: Magma<B>
        get() = forward.codomain

    override fun inverse(): MagmaIsomorphism<B, A> =
        of(backward, forward)

    companion object {
        fun <A : Any, B : Any> of(
            forward: MagmaHomomorphism<A, B>,
            backward: MagmaHomomorphism<B, A>,
        ): MagmaIsomorphism<A, B> = object : MagmaIsomorphism<A, B> {
            init {
                require(forward.codomain === backward.domain) {
                    "codomain/domain mismatch: forward.codomain != backward.domain"
                }
                require(forward.domain === backward.codomain) {}
            }

            override val forward = forward
            override val backward = backward
        }
    }
}