package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.laws.algebra.FieldLaws

object ComplexFieldSpec : StringSpec({
    "ComplexField satisfies FieldLaws" {
        FieldLaws(
            field = ComplexAlgebras.ComplexField,
            arb = ArbComplex.complex,
            eq = ComplexAlgebras.eqComplex,
            pr = ComplexAlgebras.printableComplex
        ).fullTest().throwIfFailed()
    }
})
