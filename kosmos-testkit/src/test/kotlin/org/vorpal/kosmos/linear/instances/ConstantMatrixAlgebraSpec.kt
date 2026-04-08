package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.linear.instance.arbConstMat
import org.vorpal.kosmos.core.rational.ArbRational

class ConstantMatrixAlgebraSpec : StringSpec({
    "ConstantMatrixAlgebra satisfies AlgebraLaws over Rationals" {
        val size = 5
        val algebra = ConstantMatrixAlgebras.ConstantMatrixAlgebra(
            baseField = RationalAlgebras.RationalField,
            n = size
        )

        val algebraLaws = AlgebraLaws(
            algebra = algebra,
            scalarArb = ArbRational.small,
            algebraArb = arbConstMat(ArbRational.small, size),
            eqR = RationalAlgebras.eqRational,
            eqA = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational),
            prR = RationalAlgebras.printableRationalPretty,
            prA = DenseMatAlgebras.liftPrintablePretty(RationalAlgebras.printableRationalPretty)
        )

        algebraLaws.fullTest().throwIfFailed()
    }
})
