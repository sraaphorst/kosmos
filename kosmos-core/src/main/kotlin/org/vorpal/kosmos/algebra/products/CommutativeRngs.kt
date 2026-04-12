package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRng
import org.vorpal.kosmos.algebra.structures.CommutativeSemigroup

object CommutativeRngs {
    fun <L : Any, R : Any> product(
        left: CommutativeRng<L>,
        right: CommutativeRng<R>
    ): CommutativeRng<Pair<L, R>> = object : CommutativeRng<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: CommutativeSemigroup<Pair<L, R>> = CommutativeSemigroups.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: CommutativeRng<A>
    ) = product(obj, obj)
}
