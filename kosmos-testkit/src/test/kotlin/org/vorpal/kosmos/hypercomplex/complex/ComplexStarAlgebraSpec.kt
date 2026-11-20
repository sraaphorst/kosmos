package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.RealNormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

class ComplexStarAlgebraSpec : FunSpec({
    lawTests(lawGraph("ℂ star algebra") {
        branch("ComplexStarAlgebra satisfies StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = ComplexAlgebras.ComplexStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = ArbComplex.complex,
                eqR = RealAlgebras.eqRealApprox,
                eqA = ComplexAlgebras.eqComplex,
                prR = RealAlgebras.printableRealPretty,
                prA = ComplexAlgebras.printableComplexPretty
            ))
        }

        branch("ComplexStarAlgebra satisfies RealNormedDivisionAlgebraLaws") {
            suite(RealNormedDivisionAlgebraLaws(
                algebra = ComplexAlgebras.ComplexStarAlgebra,
                arb = ArbComplex.reciprocalSafeComplex,
                eqA = ComplexAlgebras.eqComplex,
                eqReal = RealAlgebras.eqRealApprox,
                prA = ComplexAlgebras.printableComplexPretty,
                prReal = RealAlgebras.printableRealPretty
            ))
        }
    })
})
