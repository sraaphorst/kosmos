package org.vorpal.kosmos.core.ops

import org.vorpal.kosmos.core.Symbols

/** A binary operation with no prescribed properties. */
data class BinOp<A>(
    override val symbol: String = DEFAULT_SYMBOL,
    val combine: (A, A) -> A
): Op {
    constructor(combine: (A, A) -> A) : this(DEFAULT_SYMBOL, combine)

    operator fun invoke(a1: A, a2: A): A =
        combine(a1, a2)

    companion object {
        const val DEFAULT_SYMBOL = Symbols.DOT
    }
}
