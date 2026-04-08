package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.categories.Monomorphism
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRingMonomorphism<A: Any, B : Any>:
    NonAssociativeRingHomomorphism<A, B> {

    infix fun <C : Any> andThen(other: NonAssociativeRingMonomorphism<B, C>): NonAssociativeRingMonomorphism<A, C> =
        of(domain, other.codomain, map andThen other.map)

    infix fun <C : Any> compose(other: NonAssociativeRingMonomorphism<C, A>): NonAssociativeRingMonomorphism<C, B> =
        other andThen this

    companion object {
        fun <A : Any, B : Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: UnaryOp<A, B>
        ): NonAssociativeRingMonomorphism<A, B> = object : NonAssociativeRingMonomorphism<A, B> {
            override val domain: NonAssociativeRing<A> = domain
            override val codomain: NonAssociativeRing<B> = codomain
            override val map: UnaryOp<A, B> = map
        }
    }
}
