package org.vorpal.kosmos.linear.contracts

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.instances.ConstantMatrixAlgebras
import org.vorpal.kosmos.linear.ops.DenseMatOps
import org.vorpal.kosmos.linear.ops.DenseVecOps
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.PermMat
import org.vorpal.kosmos.linear.views.transposeView

/**
 * Regression tests for index checking and unit detection in the dense linear package.
 *
 * - `DenseMat.get(r, c)` checks both coordinates, instead of only the flat index `r·cols + c`.
 * - Constant-matrix and Hadamard unit tests agree with what inversion accepts (`field.hasReciprocal`).
 */
class LinearContractSpec : StringSpec({
    val real = RealAlgebras.RealField
    val m = DenseMat.ofRows(listOf(listOf(1, 2), listOf(3, 4)))

    fun <A : Any> constMat(a: A, rows: Int, cols: Int): DenseMat<A> =
        DenseMat.ofRows(List(rows) { List(cols) { a } })

    // ---------- Index checks ----------

    "DenseMat.get rejects out-of-range coordinates even when the flat index is in range" {
        shouldThrow<IndexOutOfBoundsException> { m[0, 2] }
        shouldThrow<IndexOutOfBoundsException> { m[1, -1] }
        shouldThrow<IndexOutOfBoundsException> { m[-1, 2] }
        shouldThrow<IndexOutOfBoundsException> { m[2, 0] }
        m[1, 0] shouldBe 3
        m[0, 1] shouldBe 2
    }

    "views over a DenseMat inherit the index checks" {
        val t = m.transposeView()
        t[1, 0] shouldBe 2
        shouldThrow<IndexOutOfBoundsException> { t[2, 0] }
        shouldThrow<IndexOutOfBoundsException> { t[-1, 1] }
    }

    "PermMat.get rejects out-of-range rows as well as columns" {
        val p = PermMat.identity(3)
        p[1, 1] shouldBe 1
        shouldThrow<IndexOutOfBoundsException> { p[3, 0] }
        shouldThrow<IndexOutOfBoundsException> { p[-1, 0] }
        shouldThrow<IndexOutOfBoundsException> { p[0, 3] }
    }

    // ---------- Unit detection ----------

    "ConstantMatrixField: a tiny constant is not a unit over RealField, and reciprocalOption is total" {
        val f = ConstantMatrixAlgebras.ConstantMatrixField(real, 1)
        val tiny = constMat(1e-12, 1, 1)
        f.hasReciprocal(tiny) shouldBe false
        f.reciprocalOption(tiny) shouldBe Option.None

        val two = constMat(2.0, 1, 1)
        f.hasReciprocal(two) shouldBe true
        f.reciprocal(two) shouldBe constMat(0.5, 1, 1)
    }

    "ConstantMatrixField: a wrong shape or a nonconstant matrix is simply not a unit" {
        val q = RationalAlgebras.RationalField
        val f = ConstantMatrixAlgebras.ConstantMatrixField(q, 2)
        f.hasReciprocal(constMat(Rational.ONE, 3, 3)) shouldBe false
        f.hasReciprocal(DenseMat.ofRows(listOf(listOf(Rational.ONE, Rational.ZERO), listOf(Rational.ZERO, Rational.ONE)))) shouldBe false
        f.hasReciprocal(constMat(Rational.ONE, 2, 2)) shouldBe true
        f.hasReciprocal(constMat(Rational.ZERO, 2, 2)) shouldBe false
    }

    "Hadamard units over RealField reject tiny, infinite and NaN entries; the empty vector and matrix are units" {
        DenseVecOps.isHadamardUnit(real, DenseVec.of(1e-12)) shouldBe false
        DenseVecOps.isHadamardUnit(real, DenseVec.of(Double.POSITIVE_INFINITY)) shouldBe false
        DenseVecOps.isHadamardUnit(real, DenseVec.of(Double.NaN)) shouldBe false
        DenseVecOps.isHadamardUnit(real, DenseVec.of(2.0, -3.0)) shouldBe true
        DenseVecOps.isHadamardUnit(real, DenseVec.of(emptyList<Double>())) shouldBe true

        DenseMatOps.isHadamardUnit(real, DenseMat.ofRows(listOf(listOf(1.0, 1e-12)))) shouldBe false
        DenseMatOps.isHadamardUnit(real, DenseMat.ofRows(listOf(listOf(1.0, 2.0)))) shouldBe true
    }
})
