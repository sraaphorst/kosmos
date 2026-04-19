package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.NonAssociativeSemigroup
import org.vorpal.kosmos.core.ops.pairOp

object NonAssociativeSemigroups {
    fun <L : Any, R : Any> product(
        left: NonAssociativeSemigroup<L>,
        right: NonAssociativeSemigroup<R>
    ): NonAssociativeSemigroup<Pair<L, R>> = object : NonAssociativeSemigroup<Pair<L, R>> {
        override val op = pairOp(left.op, right.op)
    }

    fun <A : Any> double(
        obj: NonAssociativeSemigroup<A>
    ): NonAssociativeSemigroup<Pair<A, A>> = product(obj, obj)
}
