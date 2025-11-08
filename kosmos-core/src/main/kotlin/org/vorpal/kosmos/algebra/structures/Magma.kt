package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

interface Magma<A: Any> {
    val op: BinOp<A>

    operator fun invoke(a1: A, a2: A): A =
        op(a1, a2)
}
