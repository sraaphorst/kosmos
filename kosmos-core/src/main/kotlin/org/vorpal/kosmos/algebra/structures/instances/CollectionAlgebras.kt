package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

val StringMonoid: Monoid<String> = Monoid.of(
    identity = "",
    op = BinOp(symbol = Symbols.PLUS) { a, b -> a + b}
)

fun <A> listMonoid(): Monoid<List<A>> = Monoid.of(
    identity = emptyList(),
    op = BinOp(symbol = Symbols.PLUS) { a, b -> a + b }
)
