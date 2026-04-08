package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

/**
 * Main structures:
 * - [StringMonoid]
 * - [listMonoid] for a given type `List<A>`.
 */
object CollectionAlgebras {
    /**
     * A [Monoid] for [String] under concatenation with the empty string as identity.
     */
    object StringMonoid: Monoid<String> {
        override val identity = ""
        override val op = BinOp(Symbols.PLUS, String::plus)
    }

    /**
     * Creates a [Monoid] for [List]s of type [A] under concatenation with the empty list as identity.
     */
    fun <A> listMonoid(): Monoid<List<A>> = Monoid.of(
        identity = emptyList(),
        op = BinOp(Symbols.PLUS, List<A>::plus)
    )
}
