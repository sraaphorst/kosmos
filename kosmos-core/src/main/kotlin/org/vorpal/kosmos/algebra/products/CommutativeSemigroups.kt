package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeSemigroup
import org.vorpal.kosmos.core.ops.pairOp

object CommutativeSemigroups {
    fun <L : Any, R : Any> product(
        left: CommutativeSemigroup<L>,
        right: CommutativeSemigroup<R>
    ): CommutativeSemigroup<Pair<L, R>> = object : CommutativeSemigroup<Pair<L, R>> {
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: CommutativeSemigroup<A>
    ): CommutativeSemigroup<Pair<A, A>> = product(obj, obj)
}
