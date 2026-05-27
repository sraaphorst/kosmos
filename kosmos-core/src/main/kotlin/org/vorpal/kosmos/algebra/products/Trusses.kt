package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.TrussHomomorphism
import org.vorpal.kosmos.algebra.structures.Truss
import org.vorpal.kosmos.core.ops.UnaryOp

object Trusses {
    fun <L : Any, R : Any> product(
        left: Truss<L>,
        right: Truss<R>
    ): Truss<Pair<L, R>> = Truss.of(
        add = AbelianHeaps.product(left.add, right.add),
        mul = Semigroups.product(left.mul, right.mul)
    )

    fun <L : Any, R : Any> leftProjection(
        left: Truss<L>,
        right: Truss<R>
    ): TrussHomomorphism<Pair<L, R>, L> = object : TrussHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Truss<L>,
        right: Truss<R>
    ): TrussHomomorphism<Pair<L, R>, R> = object : TrussHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        truss: Truss<A>
    ): Truss<Pair<A, A>> = product(truss, truss)

    fun <A : Any> diagonalEmbedding(
        truss: Truss<A>
    ): TrussHomomorphism<A, Pair<A, A>> = object : TrussHomomorphism<A, Pair<A, A>> {
        override val domain = truss
        override val codomain = double(truss)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjectionAt(
        left: Truss<L>,
        right: Truss<R>,
        rightPoint: R
    ): TrussHomomorphism<L, Pair<L, R>> = object : TrussHomomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, rightPoint) }
    }

    fun <L : Any, R : Any> rightInjectionAt(
        left: Truss<L>,
        right: Truss<R>,
        leftPoint: L
    ): TrussHomomorphism<R, Pair<L, R>> = object : TrussHomomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(leftPoint, it) }
    }
}
