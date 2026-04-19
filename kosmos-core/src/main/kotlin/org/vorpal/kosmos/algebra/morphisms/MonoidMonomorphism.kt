package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A [MonoidMonomorphism] is also a:
 * - [MonoidHomomorphism]
 * - [SemigroupMonomorphism].
 */
interface MonoidMonomorphism<A : Any, B : Any> : MonoidHomomorphism<A, B> {
    companion object {
        fun <A : Any, B : Any> of(
            domain: Monoid<A>,
            codomain: Monoid<B>,
            map: (A) -> B,
        ): MonoidMonomorphism<A, B> = object : MonoidMonomorphism<A, B> {
            override val domain = domain
            override val codomain = codomain
            override val map = UnaryOp(Symbols.PHI, map)
        }
    }
}
