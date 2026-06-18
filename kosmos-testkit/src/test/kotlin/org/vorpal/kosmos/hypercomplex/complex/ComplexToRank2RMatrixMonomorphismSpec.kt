package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras

class ComplexToRank2RMatrixMonomorphismSpec : StringSpec({
    "ComplexToRank2RMatrixMonomorphism satisfies UnitalRingHomomorphismLaws" {
        RingHomomorphismLaws(
            hom = ComplexAlgebras.ComplexToRank2RMatrixMonomorphism::invoke,
            domain = ComplexAlgebras.ComplexField,
            codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
            arb = ArbComplex.complex,
            eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
            prA = ComplexAlgebras.printableComplexPretty,
            prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
        ).fullTest().throwIfFailed()
    }

    "ComplexToRank2RMatrixMonomorphism is injective" {
        injectivityLaw(
            hom = ComplexAlgebras.ComplexToRank2RMatrixMonomorphism::invoke,
            arbA = ArbComplex.complex,
            eqA = ComplexAlgebras.eqComplex,
            eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
            prA = ComplexAlgebras.printableComplexPretty,
            prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
        ).test()
    }
})
