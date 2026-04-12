package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.pairEndo
import org.vorpal.kosmos.core.ops.pairOp

object BooleanAlgebras {
    fun <L : Any, R : Any> product(
        left: BooleanAlgebra<L>,
        right: BooleanAlgebra<R>
    ): BooleanAlgebra<Pair<L, R>> = object : BooleanAlgebra<Pair<L, R>> {
        override val join: BinOp<Pair<L, R>> = pairOp(left.join, right.join)
        override val meet: BinOp<Pair<L, R>> = pairOp(left.meet, right.meet)
        override val bottom: Pair<L, R> = Pair(left.bottom, right.bottom)
        override val top: Pair<L, R> = Pair(left.top, right.top)
        override val not: Endo<Pair<L, R>> = pairEndo(left.not, right.not)
    }

    fun <A : Any> double(
        obj: BooleanAlgebra<A>
    ) = product(obj, obj)
}
