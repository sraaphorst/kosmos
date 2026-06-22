package org.vorpal.kosmos.linear.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.ExactDivOps
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.linear.instance.arbDenseVecOfVaryingSize
import org.vorpal.kosmos.linear.instance.arbSquareDenseMatOfVaryingSize
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Tests for [DenseMatOps.detBareiss] (fraction-free Bareiss elimination), which delegates to
 * the module-internal `DenseMatKernel.detBareiss`.
 *
 * The central contract is that Bareiss agrees with the Leibniz oracle
 * [DenseMatOps.detByPermutation] on every square matrix, using exact division ([ExactDivOps]).
 * For integer matrices the Bareiss intermediate quotients are guaranteed exact, so
 * [ExactDivOps.bigInteger] never throws here.
 */
class DenseMatBareissSpec : FunSpec({

    val z = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val zDiv = ExactDivOps.bigInteger

    val q = RationalAlgebras.RationalField
    val eqQ = RationalAlgebras.eqRational
    val qDiv = ExactDivOps.fromField(q)

    fun m(vararg rows: List<Int>): DenseMat<BigInteger> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toBigInteger() } })

    fun diagProduct(mat: DenseMat<BigInteger>): BigInteger {
        var p = BigInteger.ONE
        var i = 0
        while (i < mat.rows) {
            p = p.multiply(mat[i, i])
            i += 1
        }
        return p
    }

    fun upperTriangularOf(mat: DenseMat<BigInteger>): DenseMat<BigInteger> =
        DenseMat.tabulate(mat.rows, mat.cols) { r, c -> if (c >= r) mat[r, c] else BigInteger.ZERO }

    fun lowerTriangularOf(mat: DenseMat<BigInteger>): DenseMat<BigInteger> =
        DenseMat.tabulate(mat.rows, mat.cols) { r, c -> if (c <= r) mat[r, c] else BigInteger.ZERO }

    context("equivalence with the Leibniz oracle") {
        test("detBareiss agrees with detByPermutation over ℤ") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 0, 6)) { mat ->
                eqZ(DenseMatOps.detBareiss(z, zDiv, mat), DenseMatOps.detByPermutation(z, mat)) shouldBe true
            }
        }

        test("detBareiss agrees with detByPermutation over ℚ") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbRational.small, 0, 5)) { mat ->
                eqQ(DenseMatOps.detBareiss(q, qDiv, mat), DenseMatOps.detByPermutation(q, mat)) shouldBe true
            }
        }
    }

    context("small explicit determinants over ℤ") {
        test("0×0 determinant is 1") {
            DenseMatOps.detBareiss(z, zDiv, DenseMat.tabulate(0, 0) { _, _ -> BigInteger.ZERO }) shouldBe
                BigInteger.ONE
        }

        test("1×1 determinant is its entry") {
            DenseMatOps.detBareiss(z, zDiv, m(listOf(7))) shouldBe BigInteger.valueOf(7)
        }

        test("2×2 determinant is ad − bc (and may be negative)") {
            DenseMatOps.detBareiss(z, zDiv, m(listOf(1, 2), listOf(3, 4))) shouldBe BigInteger.valueOf(-2)
        }

        test("3×3 determinant matches a known value") {
            DenseMatOps.detBareiss(z, zDiv, m(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 10))) shouldBe
                BigInteger.valueOf(-3)
        }

        test("a matrix with a negative determinant (negative diagonal entry)") {
            DenseMatOps.detBareiss(z, zDiv, m(listOf(2, 0, 0), listOf(0, 3, 0), listOf(0, 0, -1))) shouldBe
                BigInteger.valueOf(-6)
        }
    }

    context("structured matrices over ℤ") {
        test("the identity has determinant 1 at every order") {
            (0..6).forEach { n ->
                eqZ(DenseMatOps.detBareiss(z, zDiv, DenseMatOps.identity(z, n)), BigInteger.ONE) shouldBe true
            }
        }

        test("the determinant of a diagonal matrix is the product of its diagonal") {
            checkAll(arbDenseVecOfVaryingSize(ArbInteger.small, 1, 6)) { d ->
                val mat = DenseMatOps.fromDiagonal(BigInteger.ZERO, d)
                eqZ(DenseMatOps.detBareiss(z, zDiv, mat), diagProduct(mat)) shouldBe true
            }
        }

        test("the determinant of an upper-triangular matrix is the product of its diagonal") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 1, 6)) { mat ->
                val u = upperTriangularOf(mat)
                eqZ(DenseMatOps.detBareiss(z, zDiv, u), diagProduct(u)) shouldBe true
            }
        }

        test("the determinant of a lower-triangular matrix is the product of its diagonal") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 1, 6)) { mat ->
                val l = lowerTriangularOf(mat)
                eqZ(DenseMatOps.detBareiss(z, zDiv, l), diagProduct(l)) shouldBe true
            }
        }
    }

    context("singular and pivoting cases over ℤ") {
        test("a matrix with a zero column is singular") {
            val zeroCol = m(listOf(0, 1, 2), listOf(0, 3, 4), listOf(0, 5, 6))
            DenseMatOps.detBareiss(z, zDiv, zeroCol) shouldBe BigInteger.ZERO
            eqZ(DenseMatOps.detBareiss(z, zDiv, zeroCol), DenseMatOps.detByPermutation(z, zeroCol)) shouldBe true
        }

        test("a matrix with two equal rows is singular") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 2, 6)) { mat ->
                val dup = DenseMat.tabulate(mat.rows, mat.cols) { r, c -> if (r == 1) mat[0, c] else mat[r, c] }
                eqZ(DenseMatOps.detBareiss(z, zDiv, dup), BigInteger.ZERO) shouldBe true
            }
        }

        test("a zero leading pivot forces a row swap and the correct sign") {
            // (0,0) is zero, so the algorithm must swap rows; this matrix is non-singular (det = -3).
            val needsSwap = m(listOf(0, 2, 1), listOf(1, 1, 1), listOf(2, 1, 3))
            DenseMatOps.detBareiss(z, zDiv, needsSwap) shouldBe BigInteger.valueOf(-3)
            eqZ(DenseMatOps.detBareiss(z, zDiv, needsSwap), DenseMatOps.detByPermutation(z, needsSwap)) shouldBe true
        }
    }

    context("API contract") {
        test("detBareiss of a non-square matrix throws") {
            shouldThrow<IllegalArgumentException> {
                DenseMatOps.detBareiss(z, zDiv, m(listOf(1, 2, 3), listOf(4, 5, 6)))
            }
        }
    }
})
