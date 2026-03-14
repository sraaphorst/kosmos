package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.ArbGaussianRat
import org.vorpal.kosmos.hypercomplex.complex.GaussianRatAlgebras
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.linear.instance.arbConstMat

class ConstantMatrixFieldSpec : StringSpec({

    "ConstantMatrixField satisfies FieldLaws for matrices over Real" {
        val size = 8
        val field = ConstantMatrixAlgebras.ConstantMatrixField(
            base = RealAlgebras.RealField,
            n = size)
        val arb = arbConstMat(
            arbF = ArbReal.smallReal,
            n = size)
        val eq = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox)
        val pr = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)

        FieldLaws(
            field = field,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }

    "ConstantMatrixField satisfies FieldLaws for matrices over GaussianRat" {
        val size = 9
        val field = ConstantMatrixAlgebras.ConstantMatrixField(
            base = GaussianRatAlgebras.GaussianRatField,
            n = size)
        val arb = arbConstMat(
            arbF = ArbGaussianRat.small,
            n = size)
        val eq = DenseMatAlgebras.liftEq(GaussianRatAlgebras.eqGaussianRat)
        val pr = DenseMatAlgebras.liftPrintablePretty(GaussianRatAlgebras.printableGaussianRatPretty)

        FieldLaws(
            field = field,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }
})
