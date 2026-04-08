package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws

object ComplexRealVectorSpaceSpec : StringSpec({
    "ComplexRealVectorSpace satisfies VectorSpaceLaws" {
        VectorSpaceLaws(
            space = ComplexAlgebras.ComplexRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = ArbComplex.complex,
            eqF = RealAlgebras.eqRealApprox,
            eqV = ComplexAlgebras.eqComplex,
            prF = RealAlgebras.printableRealPretty,
            prV = ComplexAlgebras.printableComplexPretty
        ).fullTest().throwIfFailed()
    }
})
