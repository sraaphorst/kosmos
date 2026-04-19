package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism of some [Magma] over carrier [A] to [B].
 *
 * This extends the concept of a general [Morphism] by carrying witnesses for the domain / codomain magma structures.
 *
 * NOTE: This type does not *enforce* the homomorphism laws; it is a certified / witnessed arrow.
 *
 * Law checking is a separate concern (tests / verification helpers).
 */
interface MagmaHomomorphism<A : Any, B : Any>: Homomorphism<A, B> {
    val domain: Magma<A>
    val codomain: Magma<B>

    infix fun <C : Any> andThen(other: MagmaHomomorphism<B, C>): MagmaHomomorphism<A, C> =
        of(
            domain = domain,
            codomain = other.codomain,
            map = map andThen other.map
        )

    infix fun <C : Any> compose(other: MagmaHomomorphism<C, A>): MagmaHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: UnaryOp<A, B>
        ): MagmaHomomorphism<A, B> = object : MagmaHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = map
        }

        fun <A : Any, B : Any> of(
            domain: Magma<A>,
            codomain: Magma<B>,
            map: (A) -> B,
        ): MagmaHomomorphism<A, B> = object : MagmaHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}