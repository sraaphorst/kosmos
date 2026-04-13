package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.ops.pairOp

object Semigroups {
    fun <L : Any, R : Any> product(
        left: Semigroup<L>,
        right: Semigroup<R>
    ): Semigroup<Pair<L, R>> = object : Semigroup<Pair<L, R>> {
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: Semigroup<A>
    ) = product(obj, obj)
}
