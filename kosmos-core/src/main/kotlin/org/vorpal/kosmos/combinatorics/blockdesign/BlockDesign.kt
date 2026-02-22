package org.vorpal.kosmos.combinatorics.blockdesign

import org.vorpal.kosmos.combinatorics.arrays.Pascal
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.multiset.Multiset

/**
 * A t-(v, k, λ) block design.
 *
 * Every t-element subset of [points] is contained in exactly [lambda]
 * blocks, and every block has size [k].
 */
interface BlockDesign<T : Any> {
    val t: Int
    val k: Int
    val lambda: Int
    val points: FiniteSet<T>
    val blocks: Multiset<FiniteSet<T>>

    val v: Int
        get() = points.size

    val b: Int
        get() = blocks.size

    /** Number of blocks through any single point. */
    val r: Int
        get() = (lambda.toBigInteger() * Pascal(v - 1, t - 1) / Pascal(k - 1, t - 1)).toInt()

    /**
     * All blocks containing the given t-subset.
     * We don't impose an exact number of blocks here because we may want to work with:
     * - Covering designs or packing designs
     * - Designs that allow for
     */
    fun blocksThrough(subset: FiniteSet<T>): Multiset<FiniteSet<T>> =
        blocks.filter { block -> subset.all { it in block } }
}
