package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Given a [Semiring] over [A] and a [dimension] `n`, create a [Semiring] over `n x n` [DenseMat] of [A].
 */
class DenseMatSemiring<A : Any>(
    val entries: Semiring<A>,
    dimension: Int
): Semiring<DenseMat<A>>, MatrixDimensionality {

    init {
        DenseKernel.checkNonnegative(dimension)
    }

    override val rows = dimension
    override val cols = dimension

    override val add: CommutativeMonoid<DenseMat<A>> = DenseMatGroups.additiveCommutativeMonoid(
        monoid = entries.add,
        rows = dimension,
        cols = dimension
    )

    override val mul: Monoid<DenseMat<A>> = DenseMatGroups.multiplicativeMonoid(
        semiring = entries,
        n = dimension
    )
}
