package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism between two [Ring]s over [A] and [B].
 *
 * This extends the concept of a general [Morphism] through [NonAssociativeRingHomomorphism].
 */
interface RingHomomorphism<A: Any, B: Any> :
    NonAssociativeRingHomomorphism<A, B> {
    override val domain: Ring<A>
    override val codomain: Ring<B>

    infix fun <C: Any> andThen(other: RingHomomorphism<B, C>): RingHomomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C: Any> compose(other: RingHomomorphism<C, A>): RingHomomorphism<C, B> =
        other andThen this

    companion object {
        fun <A: Any, B: Any> of(
            domain: Ring<A>,
            codomain: Ring<B>,
            map: UnaryOp<A, B>
        ): RingHomomorphism<A, B> = object : RingHomomorphism<A, B> {
            override val domain: Ring<A> = domain
            override val codomain: Ring<B> = codomain
            override val map: UnaryOp<A, B> = map
        }

        fun <A: Any, B: Any> of(
            domain: Ring<A>,
            codomain: Ring<B>,
            map: (A) -> B
        ): RingHomomorphism<A, B> = object : RingHomomorphism<A, B> {
            override val domain: Ring<A> = domain
            override val codomain: Ring<B> = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
