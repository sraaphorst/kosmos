package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.RngHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RngMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.ops.UnaryOp

object Rngs {
    fun <L : Any, R : Any> product(
        left: Rng<L>,
        right: Rng<R>
    ): Rng<Pair<L, R>> = object : Rng<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: Semigroup<Pair<L, R>> = Semigroups.product(left.mul, right.mul)
    }

    fun <L : Any, R : Any> leftProjection(
        left: Rng<L>,
        right: Rng<R>
    ): RngHomomorphism<Pair<L, R>, L> = object : RngHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Rng<L>,
        right: Rng<R>
    ): RngHomomorphism<Pair<L, R>, R> = object : RngHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: Rng<A>
    ): Rng<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: Rng<A>
    ): RngMonomorphism<A, Pair<A, A>> = object : RngMonomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjection(
        left: Rng<L>,
        right: Rng<R>
    ): RngMonomorphism<L, Pair<L, R>> = object : RngMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, right.zero) }
    }

    fun <L : Any, R : Any> rightInjection(
        left: Rng<L>,
        right: Rng<R>
    ): RngMonomorphism<R, Pair<L, R>> = object : RngMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(left.zero, it) }
    }
}
