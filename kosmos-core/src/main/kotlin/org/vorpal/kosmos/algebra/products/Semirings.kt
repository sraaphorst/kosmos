package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring

class Semirings {
    fun <L : Any, R : Any> product(
        left: Semiring<L>,
        right: Semiring<R>
    ): Semiring<Pair<L, R>> = object : Semiring<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.add, right.add)
        override val mul: Monoid<Pair<L, R>> = Monoids.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: Semiring<A>
    ): Semiring<Pair<A, A>> = product(obj, obj)
}
