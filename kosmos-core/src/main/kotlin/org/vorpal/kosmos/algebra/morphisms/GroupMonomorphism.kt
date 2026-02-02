package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.categories.Monomorphism
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A [GroupHomomorphism] which is also a [Monomorphism].
 */
interface GroupMonomorphism<A : Any, B : Any> :
    GroupHomomorphism<A, B>,
    Monomorphism<A, B> {
        companion object {
            fun <A : Any, B : Any> of(
                domain: Group<A>,
                codomain: Group<B>,
                map: (A) -> B,
            ): GroupMonomorphism<A, B> = object : GroupMonomorphism<A, B> {
                override val domain = domain
                override val codomain = codomain
                override val map = UnaryOp(Symbols.PHI, map)
            }
        }
}
