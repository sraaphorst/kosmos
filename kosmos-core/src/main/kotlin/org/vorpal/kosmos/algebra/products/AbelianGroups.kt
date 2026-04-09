package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.ops.pairEndo
import org.vorpal.kosmos.core.ops.pairOp

object AbelianGroups {
    fun <L : Any, R : Any> product(
        left: AbelianGroup<L>,
        right: AbelianGroup<R>
    ): AbelianGroup<Pair<L, R>> = object : AbelianGroup<Pair<L, R>> {
        override val identity = Pair(left.identity, right.identity)
        override val op = pairOp(left.op, right.op)
        override val inverse = pairEndo(left.inverse, right.inverse)
    }

    fun <A : Any> double(
        obj: AbelianGroup<A>
    ) = product(obj, obj)
}