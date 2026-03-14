package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.linear.values.DenseVec

class MeanProjectSpec : StringSpec({

    "meanProject maps a rational vector to its constant mean vector" {
        val field = RationalAlgebras.RationalField
        val eq = DenseVecAlgebras.liftEq(RationalAlgebras.eqRational)

        val x = DenseVec.of(
            1.toRational(),
            2.toRational(),
            3.toRational()
        )

        val actual = ConstantMatrixAlgebras.meanProject(field, x)
        val expected = DenseVec.of(
            2.toRational(),
            2.toRational(),
            2.toRational()
        )

        eq(actual, expected) shouldBe true
    }

    "meanProject fixes constant rational vectors" {
        val field = RationalAlgebras.RationalField
        val eq = DenseVecAlgebras.liftEq(RationalAlgebras.eqRational)

        val c = 7.toRational() / 5.toRational()
        val x = DenseVec.of(c, c, c, c)

        val actual = ConstantMatrixAlgebras.meanProject(field, x)

        eq(actual, x) shouldBe true
    }

    "meanProject is idempotent over rationals" {
        val field = RationalAlgebras.RationalField
        val eq = DenseVecAlgebras.liftEq(RationalAlgebras.eqRational)

        val x = DenseVec.of(
            3.toRational(),
            (-1).toRational(),
            10.toRational(),
            0.toRational()
        )

        val once = ConstantMatrixAlgebras.meanProject(field, x)
        val twice = ConstantMatrixAlgebras.meanProject(field, once)

        eq(once, twice) shouldBe true
    }

    "meanProject preserves the total sum over rationals" {
        val field = RationalAlgebras.RationalField
        val eq = RationalAlgebras.eqRational

        val x = DenseVec.of(
            2.toRational(),
            4.toRational(),
            8.toRational(),
            10.toRational()
        )

        val projected = ConstantMatrixAlgebras.meanProject(field, x)

        var sumX = field.zero
        var sumProjected = field.zero
        var i = 0
        while (i < x.size) {
            sumX = field.add(sumX, x[i])
            sumProjected = field.add(sumProjected, projected[i])
            i += 1
        }

        eq(sumX, sumProjected) shouldBe true
    }
})