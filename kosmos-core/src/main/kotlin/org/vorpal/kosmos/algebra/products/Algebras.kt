package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.pairLeftAction

object Algebras {
    fun <R : Any, A : Any, B : Any> product(
        left: Algebra<R, A>,
        right: Algebra<R, B>
    ): Algebra<R, Pair<A, B>> = object : Algebra<R, Pair<A, B>> {
        init {
            require(left.scalars == right.scalars) { "Scalars must be the same for product algebra" }
        }
        override val scalars: CommutativeRing<R> = left.scalars
        override val add: AbelianGroup<Pair<A, B>> = AbelianGroups.product(left.add, right.add)
        override val mul: Monoid<Pair<A, B>> = Monoids.product(left.mul, right.mul)
        override val leftAction: LeftAction<R, Pair<A, B>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <R : Any, A : Any> double(
        obj: Algebra<R, A>
    ): Algebra<R, Pair<A, A>> = product(obj, obj)
}
