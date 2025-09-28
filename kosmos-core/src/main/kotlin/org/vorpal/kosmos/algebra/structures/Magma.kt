package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

interface Magma<A> {
    val op: BinOp<A>
}
