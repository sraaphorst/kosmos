package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instance.arbConstMat
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class ConstantMatrixUnitGroupSpec : FunSpec({
    lawTests(lawGraph("constant matrix unit group") {
        branch("ConstantMatrixUnitGroup satisfies AbelianGroupLaws over Real matrices") {
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

            suite(AbelianGroupLaws(
                group = group,
                arb = arb,
                eq = eq,
                pr = pr
            ))
        }

        branch("ConstantMatrixUnitGroup satisfies AbelianGroupLaws over GaussianRat matrices") {
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

            suite(AbelianGroupLaws(
                group = group,
                arb = arb,
                eq = eq,
                pr = pr
            ))
        }

        local("ConstantMatrixUnitGroup over Rationals has proper identity") {
            (1 until 10).forEach { n ->
                val entry = Rational.of(BigInteger.ONE, n.toBigInteger())
                val identity = ConstantMatrixAlgebras.ConstantMatrixUnitGroup(
                    base = RationalAlgebras.RationalField,
                    n = n
                ).identity

                (identity.size == n * n && identity.all { it == entry }) shouldBe true
            }
        }

        local("ConstantMatrixUnitGroup over Rationals multiplies to correct value") {
            val eq = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational)

            (1 until 10).forEach { n ->
                val group = ConstantMatrixAlgebras.ConstantMatrixUnitGroup(
                    base = RationalAlgebras.RationalField,
                    n = n
                )

                lawCheckAll(Arb.pair(ArbRational.nonZero, ArbRational.nonZero)) { (x, y) ->
                    val xmat = DenseMat.tabulate(n, n) { _, _ -> x }
                    val ymat = DenseMat.tabulate(n, n) { _, _ -> y }
                    val result = group.op(xmat, ymat)
                    val expected = DenseMat.tabulate(n, n) { _, _ -> x * y * n}
                    eq(result, expected) shouldBe true
                }
            }
        }

        local("ConstantMatrixUnitGroup over Rationals has proper reciprocal") {
            val eq = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational)

            (1 until 10).forEach { n ->
                val group = ConstantMatrixAlgebras.ConstantMatrixUnitGroup(
                    base = RationalAlgebras.RationalField,
                    n = n
                )

                lawCheckAll(ArbRational.nonZero) { x ->
                    val entry = (x * n * n).reciprocal()
                    val expected = DenseMat.tabulate(n, n) { _, _ -> entry }
                    val result = group.inverse(DenseMat.tabulate(n, n) { _, _ -> x } )
                    eq(result, expected) shouldBe true
                }
            }
        }
    })
})
