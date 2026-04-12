package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring

object Rings {
    fun <L : Any, R : Any> product(
        left: Ring<L>,
        right: Ring<R>
    ): Ring<Pair<L, R>> = object : Ring<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: Monoid<Pair<L, R>> = Monoids.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: Ring<A>
    ) = product(obj, obj)
}
