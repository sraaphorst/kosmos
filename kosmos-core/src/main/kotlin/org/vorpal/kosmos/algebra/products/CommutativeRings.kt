package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing

object CommutativeRings {
    fun <L : Any, R : Any> product(
        left: CommutativeRing<L>,
        right: CommutativeRing<R>
    ): CommutativeRing<Pair<L, R>> = object : CommutativeRing<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: CommutativeRing<A>
    ) = product(obj, obj)
}
