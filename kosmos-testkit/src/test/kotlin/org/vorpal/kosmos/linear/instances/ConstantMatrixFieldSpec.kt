package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.ArbGaussianRat
import org.vorpal.kosmos.hypercomplex.complex.GaussianRatAlgebras
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.linear.instance.arbConstMat
import org.vorpal.kosmos.linear.values.DenseMat

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

    "ConstantMatrixField satisfies fromBigInt for matrices over Rational" {
        val eq = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational)

        (1 until 10).forEach { n ->
            val field = ConstantMatrixAlgebras.ConstantMatrixField(
                base = RationalAlgebras.RationalField,
                n = n
            )
            val nBigInt = n.toBigInteger()

            checkAll(ArbInteger.small) { k ->
                val frac = Rational.of(k, nBigInt)
                val expected = DenseMat.tabulate(n, n) { _, _ -> frac }
                val actual = field.fromBigInt(k)
                expected shouldBe actual
            }
        }
    }
})
