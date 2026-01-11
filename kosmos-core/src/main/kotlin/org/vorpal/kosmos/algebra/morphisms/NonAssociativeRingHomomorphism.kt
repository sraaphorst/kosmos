package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

interface NonAssociativeRingHomomorphism<A: Any, B: Any> {
    val domain: NonAssociativeRing<A>
    val codomain: NonAssociativeRing<B>
    val map: UnaryOp<A, B>

    operator fun invoke(a: A): B = map(a)

    companion object {
        fun <A: Any, B: Any> of(
            domain: NonAssociativeRing<A>,
            codomain: NonAssociativeRing<B>,
            map: (A) -> B
        ): NonAssociativeRingHomomorphism<A, B> = object : NonAssociativeRingHomomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
