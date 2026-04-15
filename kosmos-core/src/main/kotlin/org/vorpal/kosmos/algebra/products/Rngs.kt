package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.algebra.structures.Semigroup

object Rngs {
    fun <L : Any, R : Any> product(
        left: Rng<L>,
        right: Rng<R>
    ): Rng<Pair<L, R>> = object : Rng<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: Semigroup<Pair<L, R>> = Semigroups.product(left.mul, right.mul)
    }

    fun <A : Any> double(
        obj: Rng<A>
    ): Rng<Pair<A, A>> = product(obj, obj)
}
