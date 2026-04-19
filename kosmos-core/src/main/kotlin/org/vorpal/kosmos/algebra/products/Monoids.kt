package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.GroupHomomorphism
import org.vorpal.kosmos.algebra.morphisms.GroupMonomorphism
import org.vorpal.kosmos.algebra.morphisms.MonoidHomomorphism
import org.vorpal.kosmos.algebra.morphisms.MonoidMonomorphism
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.ops.pairOp

object Monoids {
    fun <L : Any, R : Any> product(
        left: Monoid<L>,
        right: Monoid<R>
    ): Monoid<Pair<L, R>> = object : Monoid<Pair<L, R>> {
        override val identity: Pair<L, R> = left.identity to right.identity
        override val op = pairOp(left.op, right.op)
    }

    fun <L : Any, R : Any> leftProjection(
        left: Monoid<L>,
        right: Monoid<R>
    ): MonoidHomomorphism<Pair<L, R>, L> = object : MonoidHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Monoid<L>,
        right: Monoid<R>
    ): MonoidHomomorphism<Pair<L, R>, R> = object : MonoidHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: Monoid<A>
    ): Monoid<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: Monoid<A>
    ): MonoidMonomorphism<A, Pair<A, A>> = object : MonoidMonomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = product(obj, obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjection(
        left: Monoid<L>,
        right: Monoid<R>
    ): MonoidMonomorphism<L, Pair<L, R>> = object : MonoidMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, right.identity) }
    }

    fun <L : Any, R : Any> rightInjection(
        left: Monoid<L>,
        right: Monoid<R>
    ): MonoidMonomorphism<R, Pair<L, R>> = object : MonoidMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(left.identity, it) }
    }
}
