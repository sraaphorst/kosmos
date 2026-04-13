package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.pairOp

object Monoids {
    fun <L : Any, R : Any> product(
        left: Monoid<L>,
        right: Monoid<R>
    ): Monoid<Pair<L, R>> = object : Monoid<Pair<L, R>> {
        override val identity: Pair<L, R> = left.identity to right.identity
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: Monoid<A>
    ) = product(obj, obj)
}