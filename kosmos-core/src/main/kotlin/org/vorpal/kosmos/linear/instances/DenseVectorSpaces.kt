package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.core.ops.LeftAction

/**
 * Canonical finite-dimensional vector spaces F^n using [DenseVec] coordinates.
 */
class DenseVectorSpace<F : Any>(
    override val scalars: Field<F>,
    override val dimension: Int
): FiniteVectorSpace<F, DenseVec<F>> {
    override val add: AbelianGroup<DenseVec<F>> =
        DenseVecGroup(scalars.add, dimension)

    override val leftAction: LeftAction<F, DenseVec<F>> = LeftAction { s, v ->
        DenseKernel.requireSize(v.size, dimension)
        v.map { x -> scalars.mul(s, x) }
    }
}

fun main() {
    val field = RealAlgebras.RealField
    val space = DenseVectorSpace(field, 3)

    val a = DenseVec.of(1.0, 2.0, 3.0)
    val b = DenseVec.of(4.0, 5.0, 6.0)

    val left = space.leftAction(2.0, a)
    val right = space.leftAction(3.0, b)
    val result = space.add(left, right)
    val expected = DenseVec.of(14.0, 19.0, 24.0)
    check(result == expected) { "expected $expected but got $result" }
}
