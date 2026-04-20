package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRngHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRngMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.NonAssociativeRng
import org.vorpal.kosmos.algebra.structures.NonAssociativeSemigroup
import org.vorpal.kosmos.core.ops.UnaryOp

object NonAssociativeRngs {
    fun <L : Any, R : Any> product(
        left: NonAssociativeRng<L>,
        right: NonAssociativeRng<R>
    ): NonAssociativeRng<Pair<L, R>> = object : NonAssociativeRng<Pair<L, R>> {
        override val zero = Pair(left.zero, right.zero)
        override val add: AbelianGroup<Pair<L, R>> = AbelianGroups.product(left.add, right.add)
        override val mul: NonAssociativeSemigroup<Pair<L, R>> = NonAssociativeSemigroups.product(left.mul, right.mul)
    }

    fun <L : Any, R : Any> leftProjection(
        left: NonAssociativeRng<L>,
        right: NonAssociativeRng<R>
    ): NonAssociativeRngHomomorphism<Pair<L, R>, L> = object : NonAssociativeRngHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: NonAssociativeRng<L>,
        right: NonAssociativeRng<R>
    ): NonAssociativeRngHomomorphism<Pair<L, R>, R> = object : NonAssociativeRngHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: NonAssociativeRng<A>
    ): NonAssociativeRng<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: NonAssociativeRng<A>
    ): NonAssociativeRngMonomorphism<A, Pair<A, A>> = object : NonAssociativeRngMonomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjection(
        left: NonAssociativeRng<L>,
        right: NonAssociativeRng<R>
    ): NonAssociativeRngMonomorphism<L, Pair<L, R>> = object : NonAssociativeRngMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, right.zero) }
    }

    fun <L : Any, R : Any> rightInjection(
        left: NonAssociativeRng<L>,
        right: NonAssociativeRng<R>
    ): NonAssociativeRngMonomorphism<R, Pair<L, R>> = object : NonAssociativeRngMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(left.zero, it) }
    }
}
