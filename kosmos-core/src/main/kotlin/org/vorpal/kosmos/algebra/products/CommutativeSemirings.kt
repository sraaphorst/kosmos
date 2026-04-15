package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring

object CommutativeSemirings {
    fun <L : Any, R : Any> product(
        left: CommutativeSemiring<L>,
        right: CommutativeSemiring<R>
    ): CommutativeSemiring<Pair<L, R>> = object : CommutativeSemiring<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.add, right.add)
        override val mul: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: CommutativeSemiring<A>
    ): CommutativeSemiring<Pair<A, A>> = product(obj, obj)
}
