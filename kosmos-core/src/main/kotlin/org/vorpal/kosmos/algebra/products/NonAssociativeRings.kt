package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRngMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.ops.UnaryOp
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

    fun <L : Any, R : Any> leftProjection(
        left: NonAssociativeRing<L>,
        right: NonAssociativeRing<R>
    ): NonAssociativeRingHomomorphism<Pair<L, R>, L> = object : NonAssociativeRingHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: NonAssociativeRing<L>,
        right: NonAssociativeRing<R>
    ): NonAssociativeRingHomomorphism<Pair<L, R>, R> = object : NonAssociativeRingHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: NonAssociativeRing<A>
    ): NonAssociativeRing<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: NonAssociativeRing<A>
    ): NonAssociativeRingMonomorphism<A, Pair<A, A>> = object : NonAssociativeRingMonomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    /**
     * This has to be downgraded to a [NonAssociativeRngMonomorphism] because the element 1 is not preserved.
     */
    fun <L : Any, R : Any> leftInjection(
        left: NonAssociativeRing<L>,
        right: NonAssociativeRing<R>
    ): NonAssociativeRngMonomorphism<L, Pair<L, R>> = object : NonAssociativeRngMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, right.zero) }
    }

    /**
     * This has to be downgraded to a [NonAssociativeRngMonomorphism] because the element 1 is not preserved.
     */
    fun <L : Any, R : Any> rightInjection(
        left: NonAssociativeRing<L>,
        right: NonAssociativeRing<R>
    ): NonAssociativeRngMonomorphism<R, Pair<L, R>> = object : NonAssociativeRngMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(left.zero, it) }
    }
}
