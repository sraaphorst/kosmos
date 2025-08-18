package org.vorpal.kosmos.std

import org.vorpal.kosmos.algebra.ops.Star
import org.vorpal.kosmos.algebra.structures.CommutativeIdempotentQuasigroup
import org.vorpal.kosmos.categories.FiniteSet

/** The Fano plane (STS(7)) as a commutative idempotent quasigroup on {0..6}. */
val FanoQuasigroup: CommutativeIdempotentQuasigroup<Int, Star> by lazy {
    val points = FiniteSet.of(0..6)
    val blocks = listOf(
        Triple(0, 1, 2),
        Triple(0, 3, 4),
        Triple(0, 5, 6),
        Triple(1, 3, 5),
        Triple(1, 4, 6),
        Triple(2, 3, 6),
        Triple(2, 4, 5)
    )
    steinerFromBlocks(points, blocks)
}