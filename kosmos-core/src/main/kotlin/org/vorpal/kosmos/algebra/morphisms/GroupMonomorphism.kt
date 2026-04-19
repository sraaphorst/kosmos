package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A [GroupMonomorphism] is also a:
 * - [GroupHomomorphism]
 * - [MonoidMonomorphism].
 */
interface GroupMonomorphism<A : Any, B : Any> : GroupHomomorphism<A, B>, MonoidMonomorphism<A, B> {
        companion object {
            fun <A : Any, B : Any> of(
                domain: Group<A>,
                codomain: Group<B>,
                map: UnaryOp<A, B>,
            ): GroupMonomorphism<A, B> = object : GroupMonomorphism<A, B> {
                override val domain = domain
                override val codomain = codomain
                override val map = map
            }

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
