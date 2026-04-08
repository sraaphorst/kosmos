package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws

// TODO: ComplexStarAlgebra also implements InvolutiveRing and RealNormedDivisionAlgebra.
// TODO: We should test these as well.
object ComplexStarAlgebraSpec : StringSpec({
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
})
