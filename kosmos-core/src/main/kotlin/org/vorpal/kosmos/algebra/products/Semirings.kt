package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.SemiringHomomorphism
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.ops.UnaryOp

object Semirings {
    fun <L : Any, R : Any> product(
        left: Semiring<L>,
        right: Semiring<R>
    ): Semiring<Pair<L, R>> = object : Semiring<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val one = Pair(left.one, right.one)
        override val add: CommutativeMonoid<Pair<L, R>> = CommutativeMonoids.product(left.add, right.add)
        override val mul: Monoid<Pair<L, R>> = Monoids.product(left.mul, right.mul)
    }

    fun <L : Any, R : Any> leftProjection(
        left: Semiring<L>,
        right: Semiring<R>
    ): SemiringHomomorphism<Pair<L, R>, L> = object : SemiringHomomorphism<Pair<L, R>, L> {
        override val domain: Semiring<Pair<L, R>> = product(left, right)
        override val codomain: Semiring<L> = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Semiring<L>,
        right: Semiring<R>
    ): SemiringHomomorphism<Pair<L, R>, R> = object : SemiringHomomorphism<Pair<L, R>, R> {
        override val domain: Semiring<Pair<L, R>> = product(left, right)
        override val codomain: Semiring<R> = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: Semiring<A>
    ): Semiring<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: Semiring<A>
    ): SemiringHomomorphism<A, Pair<A, A>> = object : SemiringHomomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }
}
