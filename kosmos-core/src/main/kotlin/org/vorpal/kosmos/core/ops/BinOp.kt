package org.vorpal.kosmos.core.ops

/** A binary operation with no prescribed properties. */
data class BinOp<A>(
    val combine: (A, A) -> A,
    val symbol: String = "⋆"
)
