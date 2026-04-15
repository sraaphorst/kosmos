package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.structures.CarlstromWheel
import org.vorpal.kosmos.algebra.structures.Wheel
import org.vorpal.kosmos.core.ops.pairEndo

object Wheels {
    fun <L : Any, R : Any> product(
        left: Wheel<L>,
        right: Wheel<R>
    ): Wheel<Pair<L, R>> = object : Wheel<Pair<L, R>> {
        override val add = CommutativeMonoids.product(left.add, right.add)
        override val mul = CommutativeMonoids.product(left.mul, right.mul)
        override val inv = pairEndo(left.inv, right.inv)
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val bottom = Pair(left.bottom, right.bottom)
    }

    fun <A : Any> double(
        obj: Wheel<A>
    ): Wheel<Pair<A, A>> = product(obj, obj)
}

object CarlstromWheels {
    fun <L : Any, R : Any> product(
        left: CarlstromWheel<L>,
        right: CarlstromWheel<R>
    ): CarlstromWheel<Pair<L, R>> = object : CarlstromWheel<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add = CommutativeMonoids.product(left.add, right.add)
        override val mul = CommutativeMonoids.product(left.mul, right.mul)
        override val inv = pairEndo(left.inv, right.inv)
        override val bottom = Pair(left.bottom, right.bottom)
        override val inf = Pair(left.inf, right.inf)
    }

    fun <A : Any> double(
        obj: CarlstromWheel<A>
    ): CarlstromWheel<Pair<A, A>> = product(obj, obj)
}

