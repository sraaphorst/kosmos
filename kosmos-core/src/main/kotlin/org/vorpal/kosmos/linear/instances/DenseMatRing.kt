package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Given a [Ring] over [A] and a [dimension] `n`, create a [Ring] over `n x n` [DenseMat] of [A].
 */
class DenseMatRing<A : Any>(
    val entries: Ring<A>,
    dimension: Int
): Ring<DenseMat<A>>, MatrixDimensionality {

    init {
        DenseKernel.checkNonnegative(dimension)
    }

    override val rows = dimension
    override val cols = dimension

    override val add: AbelianGroup<DenseMat<A>> = DenseMatGroups.additiveAbelianGroup(
        group = entries.add,
        rows = dimension,
        cols = dimension
    )

    override val mul: Monoid<DenseMat<A>> = DenseMatGroups.multiplicativeMonoid(
        semiring = entries,
        n = dimension
    )
}
