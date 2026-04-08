package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism from ome [Group] over carrier [A] to [B].
 *
 * This extends the concept of a general [Morphism] by carrying witnesses for the domain / codomain group structures.
 *
 * NOTE: This type does not *enforce* the homomorphism laws; it is a certified/witnessed arrow.
 * Law checking is a separate concern (tests / verification helpers).
 */
interface GroupHomomorphism<A : Any, B : Any> : MonoidHomomorphism<A, B> {
    override val domain: Group<A>
    override val codomain: Group<B>

    infix fun <C : Any> andThen(other: GroupHomomorphism<B, C>): GroupHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: GroupHomomorphism<C, A>): GroupHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: UnaryOp<A, B>,
        ): GroupHomomorphism<A, B> = object : GroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Group<A>,
            codomain: Group<B>,
            map: (A) -> B,
        ): GroupHomomorphism<A, B> = object : GroupHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
