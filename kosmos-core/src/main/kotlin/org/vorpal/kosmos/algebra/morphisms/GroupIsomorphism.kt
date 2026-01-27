package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.categories.Isomorphism

/**
 * An isomorphism in the category of groups.
 *
 * This is both:
 * - a categorical [Isomorphism] (as plain functions on carriers), and
 * - a pair of mutually inverse [GroupHomomorphism]s with domain/codomain witnesses.
 */
interface GroupIsomorphism<A : Any, B : Any> : Isomorphism<A, B> {
    override val forward: GroupHomomorphism<A, B>
    override val backward: GroupHomomorphism<B, A>

    val domain: Group<A>
        get() = forward.domain

    val codomain: Group<B>
        get() = forward.codomain

    override fun inverse(): GroupIsomorphism<B, A> =
        of(backward, forward)

    companion object {
        fun <A : Any, B : Any> of(
            forward: GroupHomomorphism<A, B>,
            backward: GroupHomomorphism<B, A>,
        ): GroupIsomorphism<A, B> = object : GroupIsomorphism<A, B> {
            init {
                // Witness consistency checks. Use referential equality to avoid relying on structural equals.
                require(forward.codomain === backward.domain) { "codomain / domain mismatch" }
                require(forward.domain === backward.codomain) { "domain / codomain mismatch" }
            }

            override val forward = forward
            override val backward = backward
        }
    }
}
