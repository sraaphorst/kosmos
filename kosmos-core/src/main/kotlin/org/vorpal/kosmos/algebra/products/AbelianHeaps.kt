package org.vorpal.kosmos.algebra.products

import org.vorpal.kosmos.algebra.morphisms.AbelianHeapHomomorphism
import org.vorpal.kosmos.algebra.structures.AbelianHeap
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
}
