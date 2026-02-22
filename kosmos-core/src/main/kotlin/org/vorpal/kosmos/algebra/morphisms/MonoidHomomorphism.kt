package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism from ome [Monoid] over carrier [A] to [B].
 *
 * This extends the concept of a general [Morphism] by carrying witnesses for the domain / codomain monoid structures.
 *
 * NOTE: This type does not *enforce* the homomorphism laws; it is a certified/witnessed arrow.
 * Law checking is a separate concern (tests / verification helpers).
 */
interface MonoidHomomorphism<A : Any, B : Any> : AlgebraHomomorphism<A, B> {
    val domain: Monoid<A>
    val codomain: Monoid<B>

    infix fun <C : Any> andThen(other: MonoidHomomorphism<B, C>): MonoidHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MonoidHomomorphism<C, A>): MonoidHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: UnaryOp<A, B>,
        ): MonoidHomomorphism<A, B> = object : MonoidHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: (A) -> B,
        ): MonoidHomomorphism<A, B> = object : MonoidHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
