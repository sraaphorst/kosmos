package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.categories.Monomorphism
import org.vorpal.kosmos.core.ops.UnaryOp

interface RingMonomorphism<A : Any, B: Any>:
    RingHomomorphism<A, B>, NonAssociativeRingMonomorphism<A, B> {

    infix fun <C: Any> andThen(other: RingMonomorphism<B, C>): RingMonomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C: Any> compose(other: RingMonomorphism<C, A>): RingMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: Ring<A>,
            codomain: Ring<B>,
            map: UnaryOp<A, B>
        ): RingMonomorphism<A, B> = object : RingMonomorphism<A, B> {
            override val domain: Ring<A> = domain
            override val codomain: Ring<B> = codomain
            override val map: UnaryOp<A, B> = map
        }
    }
}
