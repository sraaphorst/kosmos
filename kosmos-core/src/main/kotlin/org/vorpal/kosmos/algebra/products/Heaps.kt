package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.HeapHomomorphism
import org.vorpal.kosmos.algebra.structures.Heap
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.ops.pairTernOp

object Heaps {
    fun <L : Any, R : Any> product(
        left: Heap<L>,
        right: Heap<R>
    ): Heap<Pair<L, R>> = object : Heap<Pair<L, R>> {
        override val op: TernOp<Pair<L, R>> = pairTernOp(left.op, right.op)
    }

    fun <L : Any, R : Any> leftProjection(
        left: Heap<L>,
        right: Heap<R>
    ): HeapHomomorphism<Pair<L, R>, L> = object : HeapHomomorphism<Pair<L, R>, L> {
        override val domain = product(left, right)
        override val codomain = left
        override val map = UnaryOp(Pair<L, R>::first)
    }

    fun <L : Any, R : Any> rightProjection(
        left: Heap<L>,
        right: Heap<R>
    ): HeapHomomorphism<Pair<L, R>, R> = object : HeapHomomorphism<Pair<L, R>, R> {
        override val domain = product(left, right)
        override val codomain = right
        override val map = UnaryOp(Pair<L, R>::second)
    }

    fun <A : Any> double(
        obj: Heap<A>
    ): Heap<Pair<A, A>> = product(obj, obj)
}
