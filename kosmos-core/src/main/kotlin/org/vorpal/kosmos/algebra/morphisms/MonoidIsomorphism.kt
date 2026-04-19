package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.categories.Isomorphism

interface MonoidIsomorphism<A : Any, B : Any> : MagmaIsomorphism<A, B> {
    override val forward: MonoidHomomorphism<A, B>
    override val backward: MonoidHomomorphism<B, A>

    override val domain: Monoid<A>
        get() = forward.domain
    override val codomain: Monoid<B>
        get() = forward.codomain

    override fun inverse(): MonoidIsomorphism<B, A> =
        of(backward, forward)

    companion object {
        fun <A : Any, B : Any> of(
            forward: MonoidHomomorphism<A, B>,
            backward: MonoidHomomorphism<B, A>,
        ): MonoidIsomorphism<A, B> = object : MonoidIsomorphism<A, B> {
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
