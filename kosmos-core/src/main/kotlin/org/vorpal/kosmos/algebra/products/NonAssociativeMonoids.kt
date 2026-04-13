package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.ops.pairOp

object NonAssociativeMonoids {
    fun <L : Any, R : Any> product(
        left: NonAssociativeMonoid<L>,
        right: NonAssociativeMonoid<R>
    ): NonAssociativeMonoid<Pair<L, R>> = object : NonAssociativeMonoid<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: NonAssociativeMonoid<A>
    ) = product(obj, obj)
}