package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Semialgebra
import org.vorpal.kosmos.core.linear.values.DenseMat
import org.vorpal.kosmos.core.ops.LeftAction

/**
 * The semialgebra of n×n dense matrices over a commutative semiring A.
 *
 * Scalars act by entrywise scaling: s ⊳ M = (s * m_ij)_{i,j}.
 */
class DenseMatSemialgebra<A : Any>(
    override val scalars: CommutativeSemiring<A>,
    dimension: Int
) : Semialgebra<A, DenseMat<A>>, MatrixDimensionality {

    init {
        DenseKernel.checkNonnegative(dimension)
    }

    override val rows = dimension
    override val cols = dimension

    val entries: CommutativeSemiring<A>
        get() = scalars

    override val add =
        DenseMatGroups.additiveCommutativeMonoid(
            monoid = scalars.add,
            rows = dimension,
            cols = dimension
        )

    override val mul =
        DenseMatGroups.multiplicativeMonoid(
            semiring = scalars,
            n = dimension
        )

    override val leftAction =
        LeftAction<A, DenseMat<A>> { s, m ->
            DenseMatKernel.checkSize(m, dimension, dimension)
            m.map { a -> scalars.mul(s, a) }
        }
}