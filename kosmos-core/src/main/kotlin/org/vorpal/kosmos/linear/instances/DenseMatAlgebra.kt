package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.core.ops.LeftAction

/**
 * Extend a commutative ring of a type R to a ring over n x n matrices.
 * Note that matrix multiplication is not commutative, so we cannot make this stronger than a ring.
 */
class DenseMatAlgebra<R : Any>(
    override val scalars: CommutativeRing<R>,
    dimension: Int
): Algebra<R, DenseMat<R>>, MatrixDimensionality {
    override val rows: Int = dimension
    override val cols: Int = dimension

    init {
        DenseKernel.checkNonnegative(dimension)
    }

    override val add = DenseMatGroups.additiveAbelianGroup(
        group = scalars.add,
        rows = dimension,
        cols = dimension
    )

    override val mul = DenseMatGroups.multiplicativeMonoid(
        semiring = scalars,
        dimension
    )

    override val leftAction: LeftAction<R, DenseMat<R>> =
        LeftAction { s, m -> m.map { a -> scalars.mul(s, a) } }
}
