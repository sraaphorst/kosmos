package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.pairLeftAction

object NonAssociativeAlgebras {
    fun <R : Any, A : Any, B : Any> product(
        left: NonAssociativeAlgebra<R, A>,
        right: NonAssociativeAlgebra<R, B>
    ): NonAssociativeAlgebra<R, Pair<A, B>> = object : NonAssociativeAlgebra<R, Pair<A, B>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for algebra product" }
        }
        override val scalars: CommutativeRing<R> = left.scalars
        override val add: AbelianGroup<Pair<A, B>> = AbelianGroups.product(left.add, right.add)
        override val mul: NonAssociativeMonoid<Pair<A, B>> = NonAssociativeMonoids.product(left.mul, right.mul)
        override val leftAction: LeftAction<R, Pair<A, B>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, A : Any> double(
        obj: NonAssociativeAlgebra<R, A>
    ): NonAssociativeAlgebra<R, Pair<A, A>> = product(obj, obj)
}
