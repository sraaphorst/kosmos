package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeTrussHomomorphism
import org.vorpal.kosmos.algebra.structures.NonAssociativeTruss
import org.vorpal.kosmos.core.ops.UnaryOp

object NonAssociativeTrusses {
    fun <L : Any, R : Any> product(
        left: NonAssociativeTruss<L>,
        right: NonAssociativeTruss<R>
    ): NonAssociativeTruss<Pair<L, R>> = NonAssociativeTruss.of(
        add = AbelianHeaps.product(left.add, right.add),
        mul = Magmas.product(left.mul, right.mul)
    )

    fun <L : Any, R : Any> leftProjection(
        left: NonAssociativeTruss<L>,
        right: NonAssociativeTruss<R>
    ): NonAssociativeTrussHomomorphism<Pair<L, R>, L> = object : NonAssociativeTrussHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: NonAssociativeTruss<L>,
        right: NonAssociativeTruss<R>
    ): NonAssociativeTrussHomomorphism<Pair<L, R>, R> = object : NonAssociativeTrussHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        truss: NonAssociativeTruss<A>
    ): NonAssociativeTruss<Pair<A, A>> = product(truss, truss)

    fun <A : Any> diagonalEmbedding(
        truss: NonAssociativeTruss<A>
    ): NonAssociativeTrussHomomorphism<A, Pair<A, A>> = object : NonAssociativeTrussHomomorphism<A, Pair<A, A>> {
        override val domain = truss
        override val codomain = double(truss)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjectionAt(
        left: NonAssociativeTruss<L>,
        right: NonAssociativeTruss<R>,
        rightPoint: R
    ): NonAssociativeTrussHomomorphism<L, Pair<L, R>> = object : NonAssociativeTrussHomomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, rightPoint) }
    }

    fun <L : Any, R : Any> rightInjectionAt(
        left: NonAssociativeTruss<L>,
        right: NonAssociativeTruss<R>,
        leftPoint: L
    ): NonAssociativeTrussHomomorphism<R, Pair<L, R>> = object : NonAssociativeTrussHomomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(leftPoint, it) }
    }
}
