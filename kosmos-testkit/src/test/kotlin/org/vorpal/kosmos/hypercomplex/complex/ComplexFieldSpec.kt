package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

class ComplexFieldSpec : FunSpec({
    lawTests(lawGraph("ℂ field") {
        branch("ComplexField satisfies FieldLaws") {
            suite(FieldLaws(
                field = ComplexAlgebras.ComplexField,
                arb = ArbComplex.complex,
                eq = ComplexAlgebras.eqComplex,
                pr = ComplexAlgebras.printableComplex
            ))
        }
    })
})
