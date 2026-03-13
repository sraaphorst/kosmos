package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.ArbGaussianRat
import org.vorpal.kosmos.hypercomplex.complex.GaussianRatAlgebras
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.linear.instance.arbConstMat

class ConstantMatrixUnitGroupSpec : StringSpec({

    "ConstantMatrixUnitGroup satisfies AbelianGroupLaws over Real matrices" {
        val sizeR = 5
        val group = ConstantMatrixAlgebras.ConstantMatrixUnitGroup(
            base = RealAlgebras.RealField,
            n = sizeR
        )
        val arb = arbConstMat(
            arbF = ArbReal.nonZeroReal,
            n = sizeR
        )
        val eq = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox)
        val pr = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)

        AbelianGroupLaws(
            group = group,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }


    "ConstantMatrixUnitGroup satisfies AbelianGroupLaws over GaussianRat matrices" {
        val sizeCQ = 6
        val group = ConstantMatrixAlgebras.ConstantMatrixUnitGroup(
            base = GaussianRatAlgebras.GaussianRatField,
            n = sizeCQ
        )
        val arb = arbConstMat(
            arbF = ArbGaussianRat.nonzero,
            n = sizeCQ
        )
        val eq = DenseMatAlgebras.liftEq(GaussianRatAlgebras.eqGaussianRat)
        val pr = DenseMatAlgebras.liftPrintablePretty(GaussianRatAlgebras.printableGaussianRatPretty)

        AbelianGroupLaws(
            group = group,
            arb = arb,
            eq = eq,
            pr = pr
        ).fullTest().throwIfFailed()
    }
})
