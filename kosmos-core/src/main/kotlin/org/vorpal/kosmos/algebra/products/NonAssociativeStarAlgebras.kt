package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.pairEndo
import org.vorpal.kosmos.core.ops.pairLeftAction

object NonAssociativeStarAlgebras {
    fun <R : Any, A : Any, B : Any> product(
        left: NonAssociativeStarAlgebra<R, A>,
        right: NonAssociativeStarAlgebra<R, B>
    ): NonAssociativeStarAlgebra<R, Pair<A, B>> = object : NonAssociativeStarAlgebra<R, Pair<A, B>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for star algebra product" }
        }
        override val one: Pair<A, B> = Pair(left.one, right.one)
        override val scalars: CommutativeRing<R> = left.scalars
        override val add: AbelianGroup<Pair<A, B>> = AbelianGroups.product(left.add, right.add)
        override val mul: NonAssociativeMonoid<Pair<A, B>> = NonAssociativeMonoids.product(left.mul, right.mul)
        override val conj: Endo<Pair<A, B>> = pairEndo(left.conj, right.conj)
        override val leftAction: LeftAction<R, Pair<A, B>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, A : Any> double(
        obj: NonAssociativeStarAlgebra<R, A>
    ): NonAssociativeStarAlgebra<R, Pair<A, A>> = product(obj, obj)
}
