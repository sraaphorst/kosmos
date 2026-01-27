package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.instances.DenseVecAlgebras.DenseVectorSpace
import org.vorpal.kosmos.linear.values.DenseVec

object AnalysisReal {
    val field = RealAlgebras.RealField

    fun rn(n: Int): DenseVectorSpace<Real> =
        DenseVectorSpace(field, n)

    fun vec(vararg xs: Real): DenseVec<Real> =
        DenseVec.of(*xs.toTypedArray())
}
