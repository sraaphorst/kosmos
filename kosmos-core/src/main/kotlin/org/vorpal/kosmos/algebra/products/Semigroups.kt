package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.ops.pairBinOp

object Semigroups {
    fun <L : Any, R : Any> product(
        left: Semigroup<L>,
        right: Semigroup<R>
    ): Semigroup<Pair<L, R>> = object : Semigroup<Pair<L, R>> {
        override val op = pairBinOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: Semigroup<A>
    ): Semigroup<Pair<A, A>> = product(obj, obj)
}
