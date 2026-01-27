package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism from one [NonAssociativeRing] over [A] ](i.e. not guaranteed to be associative) to another over [B].
 *
 * Note that this extends the concept of a [Morphism] from the category module.
 */
interface NonAssociativeRingHomomorphism<A: Any, B: Any> : Morphism<A, B> {
    val domain: NonAssociativeRing<A>
    val codomain: NonAssociativeRing<B>
    val map: UnaryOp<A, B>

    override fun apply(a: A): B = map(a)
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
