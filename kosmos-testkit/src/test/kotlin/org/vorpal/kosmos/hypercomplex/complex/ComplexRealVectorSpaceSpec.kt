package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

class ComplexRealVectorSpaceSpec : FunSpec({
    lawTests(lawGraph("ℂ as an ℝ-vector space") {
        branch("ComplexRealVectorSpace satisfies VectorSpaceLaws") {
            suite(VectorSpaceLaws(
                space = ComplexAlgebras.ComplexRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = ArbComplex.complex,
                eqF = RealAlgebras.eqRealApprox,
                eqV = ComplexAlgebras.eqComplex,
                prF = RealAlgebras.printableRealPretty,
                prV = ComplexAlgebras.printableComplexPretty
            ))
        }
    })
})
