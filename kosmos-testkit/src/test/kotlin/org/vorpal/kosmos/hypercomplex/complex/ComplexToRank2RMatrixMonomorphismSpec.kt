package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.testing.laws.lawTests

class ComplexToRank2RMatrixMonomorphismSpec : FunSpec({
    lawTests(lawGraph("ℂ ↪ M₂(ℝ)") {
        branch("ComplexToRank2RMatrixMonomorphism satisfies UnitalRingHomomorphismLaws") {
            suite(RingHomomorphismLaws(
                hom = ComplexAlgebras.ComplexToRank2RMatrixMonomorphism::invoke,
                domain = ComplexAlgebras.ComplexField,
                codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
                arb = ArbComplex.complex,
                eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
                prA = ComplexAlgebras.printableComplexPretty,
                prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
            ))
        }

        law("ComplexToRank2RMatrixMonomorphism is injective", injectivityLaw(
                hom = ComplexAlgebras.ComplexToRank2RMatrixMonomorphism::invoke,
                arbA = ArbComplex.complex,
                eqA = ComplexAlgebras.eqComplex,
                eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
                prA = ComplexAlgebras.printableComplexPretty,
                prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
            ))
    })
})
