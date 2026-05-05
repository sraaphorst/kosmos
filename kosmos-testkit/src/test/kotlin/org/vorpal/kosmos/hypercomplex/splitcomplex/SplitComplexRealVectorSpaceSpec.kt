package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws

/**
 * Mirrors `ComplexRealVectorSpaceSpec`.
 *
 * `SplitComplexRealVectorSpace` is the two-dimensional `Real`-vector space
 * underlying `SplitComplex<Real>`. The vector-space axioms are exactly the
 * `R`-module axioms over a field, so this is essentially the module spec
 * with a stronger scalar requirement.
 */
object SplitComplexRealVectorSpaceSpec : StringSpec({
    "SplitComplexRealVectorSpace satisfies VectorSpaceLaws" {
        VectorSpaceLaws(
            space = SplitComplexAlgebras.SplitComplexRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = ArbSplitComplex.smallSplitComplex,
            eqF = RealAlgebras.eqRealApprox,
            eqV = SplitComplexAlgebras.eqSplitComplex,
            prF = RealAlgebras.printableRealPretty,
            prV = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
