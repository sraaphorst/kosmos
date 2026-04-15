package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import java.math.BigInteger

object NonAssociativeRings {
    fun <L : Any, R : Any> product(
        left: NonAssociativeRing<L>,
        right: NonAssociativeRing<R>
    ): NonAssociativeRing<Pair<L, R>> = object : NonAssociativeRing<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: NonAssociativeMonoid<Pair<L, R>> = NonAssociativeMonoids.product(left.mul, right.mul)

        override fun fromBigInt(n: BigInteger): Pair<L, R> =
            Pair(left.fromBigInt(n), right.fromBigInt(n))
    }

    fun <A : Any> double(
        obj: NonAssociativeRing<A>
    ): NonAssociativeRing<Pair<A, A>> = product(obj, obj)
}
