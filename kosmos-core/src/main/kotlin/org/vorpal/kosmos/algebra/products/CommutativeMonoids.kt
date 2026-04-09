package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.core.ops.pairOp

object CommutativeMonoids {
    fun <L : Any, R : Any> product(
        left: CommutativeMonoid<L>,
        right: CommutativeMonoid<R>
    ): CommutativeMonoid<Pair<L, R>> = object : CommutativeMonoid<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: CommutativeMonoid<A>
    ) = product(obj, obj)
}
