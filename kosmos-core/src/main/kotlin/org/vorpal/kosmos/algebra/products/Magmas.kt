package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.core.ops.pairOp

object Magmas {
    fun <L : Any, R : Any> product(
        left: Magma<L>,
        right: Magma<R>
    ): Magma<Pair<L, R>> = object : Magma<Pair<L, R>> {
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: Magma<A>
    ) = product(obj, obj)
}
