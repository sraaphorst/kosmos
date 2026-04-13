package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.ops.pairOp

object Loops {
    fun <L : Any, R : Any> product(
        left: Loop<L>,
        right: Loop<R>
    ): Loop<Pair<L, R>> = object : Loop<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairOp(left.op, right.op)
        override val leftDiv = pairOp(left.leftDiv, right.leftDiv)
        override val rightDiv = pairOp(left.rightDiv, right.rightDiv)
    }

    fun <A : Any> double(
        obj: Loop<A>
    ) = product(obj, obj)
}