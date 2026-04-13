package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.pairEndo

object NonAssociativeInvolutiveRings {
    fun <L : Any, R : Any> product(
        left: NonAssociativeInvolutiveRing<L>,
        right: NonAssociativeInvolutiveRing<R>
    ): NonAssociativeInvolutiveRing<Pair<L, R>> = object : NonAssociativeInvolutiveRing<Pair<L, R>> {
        override val zero: Pair<L, R> = Pair(left.zero, right.zero)
        override val one: Pair<L, R> = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: NonAssociativeMonoid<Pair<L, R>> = NonAssociativeMonoids.product(left.mul, right.mul)
        override val conj: Endo<Pair<L, R>> = pairEndo(left.conj, right.conj)
    }

    fun <A : Any> double(
        obj: NonAssociativeInvolutiveRing<A>
    ) = product(obj, obj)
}
