package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A Magma is the simplest structure: a set with a closed binary operation.
 *
 * There is no guarantee of associativity or commutativity.
 */
interface Magma<A: Any> {
    val op: BinOp<A>

    operator fun invoke(a1: A, a2: A): A =
        op(a1, a2)
}
