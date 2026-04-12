package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Hemiring
import org.vorpal.kosmos.algebra.structures.Semigroup

object Hemirings {
    fun <L : Any, R : Any> product(
        left: Hemiring<L>,
        right: Hemiring<R>
    ): Hemiring<Pair<L, R>> = object : Hemiring<Pair<L, R>> {
        override val add: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.add, right.add)
        override val mul: Semigroup<Pair<L, R>> = Semigroups.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: Hemiring<A>
    ) = product(obj, obj)
}
