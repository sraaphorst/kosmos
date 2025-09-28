package org.vorpal.kosmos.algorithms.combinatorial

import org.vorpal.kosmos.combinatorial.FiniteSet

fun <A> FiniteSet.Ordered<A>.generateAllSubsets(): Sequence<FiniteSet.Ordered<A>> = sequence {
    val n = size
    (0 until (1 shl n)).asSequence().forEach { mask ->
        val subset = order.withIndex()
            .filter { (i, _) -> (mask and (1 shl i)) != 0 }
            .map { it.value }
        yield(FiniteSet.ordered(subset))
    }
}

fun <A> FiniteSet.Ordered<A>.generateAllKSubsets(k: Int): Sequence<FiniteSet.Ordered<A>> {
    require(k in 0..size) { "$k must be between 0 and $size" }

    fun go(start: Int = 0, s: Int = k): Sequence<List<A>> =
        when {
            s == 0 -> sequenceOf(emptyList())
            start == size -> emptySequence()
            else -> {
                val head = order[start]
                val withHead = go(start + 1, s - 1).map { listOf(head) + it }
                val withoutHead = go(start + 1, s)
                withHead + withoutHead
            }
        }

    return go().map { FiniteSet.ordered(it) }
}