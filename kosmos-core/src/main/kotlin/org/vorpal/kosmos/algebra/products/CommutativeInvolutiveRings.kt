package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.pairEndo

object CommutativeInvolutiveRings {
    fun <L : Any, R : Any> product(
        left: CommutativeInvolutiveRing<L>,
        right: CommutativeInvolutiveRing<R>
    ): CommutativeInvolutiveRing<Pair<L, R>> = object : CommutativeInvolutiveRing<Pair<L, R>> {
        override val zero: Pair<L, R> = Pair(left.zero, right.zero)
        override val one: Pair<L, R> = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.mul, right.mul)
        override val conj: Endo<Pair<L, R>> = pairEndo(left.conj, right.conj)
    }

    fun <A : Any> double(
        obj: InvolutiveRing<A>
    ): InvolutiveRing<Pair<A, A>> = InvolutiveRings.product(obj, obj)
}
