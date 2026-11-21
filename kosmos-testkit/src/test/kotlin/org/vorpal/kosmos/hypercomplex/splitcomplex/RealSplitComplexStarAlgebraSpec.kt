package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Mirrors `ComplexStarAlgebraSpec`.
 *
 * `RealSplitComplexStarAlgebra` is a `StarAlgebra<Real, SplitComplex<Real>>`,
 * but unlike `ComplexStarAlgebra` it is *not* a `RealNormedDivisionAlgebra`
 * (split-complex numbers have zero divisors and the quadratic form is indefinite).
 * So we only verify the abstract `StarAlgebra` laws here.
 */
class RealSplitComplexStarAlgebraSpec : FunSpec({
    lawTests(lawGraph("split-complex star algebra") {
        branch("RealSplitComplexStarAlgebra satisfies StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = SplitComplexAlgebras.RealSplitComplexStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = ArbSplitComplex.smallSplitComplex,
                eqR = RealAlgebras.eqRealApprox,
                eqA = SplitComplexAlgebras.eqSplitComplex,
                prR = RealAlgebras.printableRealPretty,
                prA = SplitComplexAlgebras.printableSplitComplexPretty
            ))
        }
    })
})
