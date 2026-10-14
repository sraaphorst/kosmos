package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.RealNormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws

class ComplexStarAlgebraSpec : StringSpec({
    "ComplexStarAlgebra satisfies StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = ComplexAlgebras.ComplexStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = ArbComplex.complex,
            eqR = RealAlgebras.eqRealApprox,
            eqA = ComplexAlgebras.eqComplex,
            prR = RealAlgebras.printableRealPretty,
            prA = ComplexAlgebras.printableComplexPretty
        ).fullTest().throwIfFailed()
    }

    "ComplexStarAlgebra satisfies RealNormedDivisionAlgebraLaws" {
        RealNormedDivisionAlgebraLaws(
            algebra = ComplexAlgebras.ComplexStarAlgebra,
            arb = ArbComplex.reciprocalSafeComplex,
            eqA = ComplexAlgebras.eqComplex,
            eqReal = RealAlgebras.eqRealApprox,
            prA = ComplexAlgebras.printableComplexPretty,
            prReal = RealAlgebras.printableRealPretty
        ).fullTest().throwIfFailed()
    }
})
