package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws

/**
 * Mirrors `ComplexStarAlgebraSpec`.
 *
 * `RealSplitComplexStarAlgebra` is a `StarAlgebra<Real, SplitComplex<Real>>`,
 * but unlike `ComplexStarAlgebra` it is *not* a `RealNormedDivisionAlgebra`
 * (split-complex numbers have zero divisors and the quadratic form is indefinite).
 * So we only verify the abstract `StarAlgebra` laws here.
 */
class RealSplitComplexStarAlgebraSpec : StringSpec({
    "RealSplitComplexStarAlgebra satisfies StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = SplitComplexAlgebras.RealSplitComplexStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = ArbSplitComplex.smallSplitComplex,
            eqR = RealAlgebras.eqRealApprox,
            eqA = SplitComplexAlgebras.eqSplitComplex,
            prR = RealAlgebras.printableRealPretty,
            prA = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
