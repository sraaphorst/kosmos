package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.pairEndo

object Meadows {
    fun <L : Any, R : Any> product(
        left: Meadow<L>,
        right: Meadow<R>
    ): Meadow<Pair<L, R>> = object : Meadow<Pair<L, R>> {
        override val zero: Pair<L, R> = Pair(left.zero, right.zero)
        override val one: Pair<L, R> = Pair(left.one, right.one)

        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.mul, right.mul)
        override val inv: Endo<Pair<L, R>> = pairEndo(left.inv, right.inv)
    }

    fun <A : Any> double(
        obj: Meadow<A>
    ) = product(obj, obj)
}
