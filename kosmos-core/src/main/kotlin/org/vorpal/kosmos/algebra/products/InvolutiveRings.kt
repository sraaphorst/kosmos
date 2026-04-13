package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.pairEndo

object InvolutiveRings {
    fun <L : Any, R : Any> product(
        left: InvolutiveRing<L>,
        right: InvolutiveRing<R>
    ): InvolutiveRing<Pair<L, R>> = object : InvolutiveRing<Pair<L, R>> {
        override val zero: Pair<L, R> = Pair(left.zero, right.zero)
        override val one: Pair<L, R> = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: Monoid<Pair<L, R>> = Monoids.product(left.mul, right.mul)
        override val conj: Endo<Pair<L, R>> = pairEndo(left.conj, right.conj)
    }

    fun <A : Any> double(
        obj: InvolutiveRing<A>
    ) = product(obj, obj)
}
