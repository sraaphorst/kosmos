package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.ops.pairBinOp

object Loops {
    fun <L : Any, R : Any> product(
        left: Loop<L>,
        right: Loop<R>
    ): Loop<Pair<L, R>> = object : Loop<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairBinOp(left.op, right.op)
        override val leftDiv = pairBinOp(left.leftDiv, right.leftDiv)
        override val rightDiv = pairBinOp(left.rightDiv, right.rightDiv)
    }

    fun <A : Any> double(
        obj: Loop<A>
    ): Loop<Pair<A, A>> = product(obj, obj)
}
