package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semialgebra
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.pairLeftAction

object Semialgebras {
    fun <S : Any, A : Any, B : Any> product(
        left: Semialgebra<S, A>,
        right: Semialgebra<S, B>
    ): Semialgebra<S, Pair<A, B>> = object : Semialgebra<S, Pair<A, B>> {
        init {
            require(left.scalars === right.scalars) { "Scalars must be the same for semialgebra product" }
        }
        override val scalars: CommutativeSemiring<S> = left.scalars
        override val add: CommutativeMonoid<Pair<A, B>> = CommutativeMonoids.product(left.add, right.add)
        override val mul: Monoid<Pair<A, B>> = Monoids.product(left.mul, right.mul)
        override val leftAction: LeftAction<S, Pair<A, B>> = pairLeftAction(left.leftAction, right.leftAction)
    }

    fun <S : Any, A : Any> double(
        obj: Semialgebra<S, A>
    ): Semialgebra<S, Pair<A, A>> = product(obj, obj)
}