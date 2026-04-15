package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Quasigroup
import org.vorpal.kosmos.core.ops.pairOp

object Quasigroups {
    fun <L : Any, R : Any> product(
        left: Quasigroup<L>,
        right: Quasigroup<R>
    ): Quasigroup<Pair<L, R>> = object : Quasigroup<Pair<L, R>> {
        override val op = pairOp(left.op, right.op)
        override val leftDiv = pairOp(left.leftDiv, right.leftDiv)
        override val rightDiv = pairOp(left.rightDiv, right.rightDiv)
    }

    fun <A : Any> double(
        obj: Quasigroup<A>
    ): Quasigroup<Pair<A, A>> = product(obj, obj)
}
