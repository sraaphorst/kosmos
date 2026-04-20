package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.morphisms.RngMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

object Rings {
    fun <L : Any, R : Any> product(
        left: Ring<L>,
        right: Ring<R>
    ): Ring<Pair<L, R>> = object : Ring<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: Monoid<Pair<L, R>> = Monoids.product(left.mul, right.mul)

        override fun fromBigInt(n: BigInteger): Pair<L, R> =
            Pair(left.fromBigInt(n), right.fromBigInt(n))
    }

    fun <L : Any, R : Any> leftProjection(
        left: Ring<L>,
        right: Ring<R>
    ): RingHomomorphism<Pair<L, R>, L> = object : RingHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Ring<L>,
        right: Ring<R>
    ): RingHomomorphism<Pair<L, R>, R> = object : RingHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: Ring<A>
    ): Ring<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: Ring<A>
    ): RingMonomorphism<A, Pair<A, A>> = object : RingMonomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    /**
     * This has to be downgraded to a [RngMonomorphism] because the element 1 is not preserved.
     */
    fun <L : Any, R : Any> leftInjection(
        left: Ring<L>,
        right: Ring<R>
    ): RngMonomorphism<L, Pair<L, R>> = object : RngMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, right.zero) }
    }

    /**
     * This has to be downgraded to a [RngMonomorphism] because the element 1 is not preserved.
     */
    fun <L : Any, R : Any> rightInjection(
        left: Ring<L>,
        right: Ring<R>
    ): RngMonomorphism<R, Pair<L, R>> = object : RngMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(left.zero, it) }
    }
}
