package org.vorpal.kosmos.analysis.instances

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.linear.instances.DenseVectorSpace
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.core.math.Real

object AnalysisReal {
    val field = RealAlgebras.RealField

    fun rn(n: Int): org.vorpal.kosmos.linear.instances.DenseVectorSpace<Real> =
        _root_ide_package_.org.vorpal.kosmos.linear.instances.DenseVectorSpace(field, n)

    fun vec(vararg xs: Real): org.vorpal.kosmos.linear.values.DenseVec<Real> =
        _root_ide_package_.org.vorpal.kosmos.linear.values.DenseVec.of(*xs.toTypedArray())
}
