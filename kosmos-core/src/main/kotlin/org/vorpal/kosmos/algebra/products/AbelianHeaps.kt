package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.AbelianHeapHomomorphism
import org.vorpal.kosmos.algebra.morphisms.AbelianHeapMonomorphism
import org.vorpal.kosmos.algebra.morphisms.HeapHomomorphism
import org.vorpal.kosmos.algebra.morphisms.HeapMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.algebra.structures.Heap
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.ops.pairTernOp

object AbelianHeaps {
    fun <L : Any, R : Any> product(
        left: AbelianHeap<L>,
        right: AbelianHeap<R>
    ): AbelianHeap<Pair<L, R>> = object : AbelianHeap<Pair<L, R>> {
        override val op: TernOp<Pair<L, R>> = pairTernOp(left.op, right.op)
    }

    fun <L : Any, R : Any> leftProjection(
        left: AbelianHeap<L>,
        right: AbelianHeap<R>
    ): AbelianHeapHomomorphism<Pair<L, R>, L> = object : AbelianHeapHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: AbelianHeap<L>,
        right: AbelianHeap<R>
    ): AbelianHeapHomomorphism<Pair<L, R>, R> = object : AbelianHeapHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: AbelianHeap<A>
    ): AbelianHeap<Pair<A, A>> = product(obj, obj)

    fun <A : Any> diagonalEmbedding(
        obj: AbelianHeap<A>
    ): AbelianHeapHomomorphism<A, Pair<A, A>> = object : AbelianHeapHomomorphism<A, Pair<A, A>> {
        override val domain = obj
        override val codomain = double(obj)
        override val map = UnaryOp<A, Pair<A, A>> { Pair(it, it) }
    }

    fun <L : Any, R : Any> leftInjectionAt(
        left: AbelianHeap<L>,
        right: AbelianHeap<R>,
        rightPoint: R
    ): AbelianHeapMonomorphism<L, Pair<L, R>> = object : AbelianHeapMonomorphism<L, Pair<L, R>> {
        override val domain = left
        override val codomain = product(left, right)
        override val map = UnaryOp<L, Pair<L, R>> { Pair(it, rightPoint) }
    }

    fun <L : Any, R : Any> rightInjectionAt(
        left: AbelianHeap<L>,
        right: AbelianHeap<R>,
        leftPoint: L
    ): AbelianHeapMonomorphism<R, Pair<L, R>> = object : AbelianHeapMonomorphism<R, Pair<L, R>> {
        override val domain = right
        override val codomain = product(left, right)
        override val map = UnaryOp<R, Pair<L, R>> { Pair(leftPoint, it) }
    }
}
