package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.AlgebraLaws

/**
 * Property tests for `SplitComplexAlgebras.splitComplexAlgebra(RealField)`,
 * the canonical `Algebra<Real, SplitComplex<Real>>`.
 *
 * The algebra and star-algebra structures share the same underlying ring
 * and scalar action, but it is still useful to exercise the plain `Algebra`
 * laws independently of conjugation.
 */
class SplitComplexRealAlgebraSpec : StringSpec({
    "splitComplexAlgebra(Real) satisfies AlgebraLaws" {
        AlgebraLaws(
            algebra = SplitComplexAlgebras.splitComplexAlgebra(RealAlgebras.RealField),
            scalarArb = ArbReal.smallReal,
            algebraArb = ArbSplitComplex.smallSplitComplex,
            eqR = RealAlgebras.eqRealApprox,
            eqA = SplitComplexAlgebras.eqSplitComplex,
            prR = RealAlgebras.printableRealPretty,
            prA = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
