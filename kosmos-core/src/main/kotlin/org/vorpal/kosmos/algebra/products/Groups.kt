package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.ops.pairEndo
import org.vorpal.kosmos.core.ops.pairOp

object Groups {
    fun <L : Any, R : Any> product(
        left: Group<L>,
        right: Group<R>
    ): Group<Pair<L, R>> = object : Group<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairOp(left.op, right.op)
        override val inverse = pairEndo(left.inverse, right.inverse)
    }

    fun <A : Any> double(
        obj: Group<A>
    ) = product(obj, obj)
}
