package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RngHomomorphismLaws
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras

object ComplexToRank2RMatrixMonomorphismSpec : StringSpec({
    "ComplexToRank2RMatrixMonomorphism satisfies RingHomomorphismLaws" {
        RngHomomorphismLaws(
            hom = ComplexAlgebras.ComplexToRank2RMatrixMonomorphism::invoke,
            domain = ComplexAlgebras.ComplexField,
            codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
            arb = ArbComplex.complex,
            eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
            prA = ComplexAlgebras.printableComplexPretty,
            prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
        ).fullTest().throwIfFailed()
    }
})
