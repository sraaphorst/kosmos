package org.vorpal.kosmos.linear.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.linear.instance.arbPermMatOfVaryingSize
import org.vorpal.kosmos.linear.instance.arbSquareDenseMatOfVaryingSize
import org.vorpal.kosmos.linear.instance.arbSquareDenseMatPair
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.PermMat
import org.vorpal.kosmos.linear.views.transposeView
import java.math.BigInteger

/**
 * Tests for [DenseMatOps.det] and [DenseMatOps.detByPermutation].
 *
 * [DenseMatOps.det] currently delegates to [DenseMatOps.detByPermutation], which in turn calls
 * the (module-internal) `DenseMatKernel.detByPermutation`. Since the kernel is `internal`, it is
 * exercised here through its public wrappers, which is also the contract callers depend on.
 *
 * We test over two commutative rings:
 * - the integers `ℤ` ([IntegerAlgebras.IntegerCommutativeRing]), and
 * - the rationals `ℚ` ([RationalAlgebras.RationalField]).
 */
class DenseMatDeterminantSpec : FunSpec({

    val z = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger

    val q = RationalAlgebras.RationalField
    val eqQ = RationalAlgebras.eqRational

    fun zMat(vararg rows: List<Int>): DenseMat<BigInteger> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toBigInteger() } })

    fun diagProduct(m: DenseMat<BigInteger>): BigInteger {
        var p = BigInteger.ONE
        var i = 0
        while (i < m.rows) {
            p = p.multiply(m[i, i])
            i += 1
        }
        return p
    }

    fun upperTriangularOf(m: DenseMat<BigInteger>): DenseMat<BigInteger> =
        DenseMat.tabulate(m.rows, m.cols) { r, c -> if (c >= r) m[r, c] else BigInteger.ZERO }

    fun permToZMat(pm: PermMat): DenseMat<BigInteger> =
        DenseMat.tabulate(pm.rows, pm.cols) { r, c -> pm[r, c].toBigInteger() }

    context("small explicit determinants over ℤ") {
        test("the empty 0×0 determinant is the multiplicative identity (1)") {
            DenseMatOps.det(z, DenseMat.tabulate(0, 0) { _, _ -> BigInteger.ZERO }) shouldBe BigInteger.ONE
        }

        test("a 1×1 determinant is its single entry") {
            DenseMatOps.det(z, zMat(listOf(7))) shouldBe BigInteger.valueOf(7)
        }

        test("a 2×2 determinant is ad − bc") {
            DenseMatOps.det(z, zMat(listOf(1, 2), listOf(3, 4))) shouldBe BigInteger.valueOf(-2)
        }

        test("a 3×3 determinant matches cofactor expansion") {
            // det = -3 by hand.
            DenseMatOps.det(z, zMat(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 10))) shouldBe
                BigInteger.valueOf(-3)
        }

        test("a 4×4 determinant matches a known value") {
            // Block-diagonal of [[1,2],[3,4]] (det -2) and [[2,0],[0,3]] (det 6) => -12.
            val m = zMat(
                listOf(1, 2, 0, 0),
                listOf(3, 4, 0, 0),
                listOf(0, 0, 2, 0),
                listOf(0, 0, 0, 3),
            )
            DenseMatOps.det(z, m) shouldBe BigInteger.valueOf(-12)
        }
    }

    context("small explicit determinants over ℚ") {
        test("a 2×2 rational determinant") {
            val half = 1.toRational() / 2.toRational()
            val third = 1.toRational() / 3.toRational()
            val quarter = 1.toRational() / 4.toRational()
            val fifth = 1.toRational() / 5.toRational()

            val m = DenseMat.ofRows(
                listOf(
                    listOf(half, third),
                    listOf(quarter, fifth),
                )
            )
            // (1/2)(1/5) - (1/3)(1/4) = 1/10 - 1/12 = 1/60.
            eqQ(DenseMatOps.det(q, m), 1.toRational() / 60.toRational()) shouldBe true
        }

        test("the rational identity has determinant 1") {
            eqQ(DenseMatOps.det(q, DenseMatOps.identity(q, 4)), Rational.ONE) shouldBe true
        }
    }

    context("structural laws over ℤ") {
        test("det(I_n) = 1 for every order") {
            (0..6).forEach { n ->
                eqZ(DenseMatOps.det(z, DenseMatOps.identity(z, n)), BigInteger.ONE) shouldBe true
            }
        }

        test("det is invariant under transpose") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 0, 5)) { m ->
                eqZ(DenseMatOps.det(z, m), DenseMatOps.det(z, m.transposeView())) shouldBe true
            }
        }

        test("det is multiplicative: det(AB) = det(A)·det(B)") {
            checkAll(arbSquareDenseMatPair(ArbInteger.small, 0, 4)) { (a, b) ->
                val lhs = DenseMatOps.det(z, DenseMatOps.matMul(z, a, b))
                val rhs = z.mul(DenseMatOps.det(z, a), DenseMatOps.det(z, b))
                eqZ(lhs, rhs) shouldBe true
            }
        }

        test("the determinant of an upper-triangular matrix is the product of its diagonal") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 1, 5)) { m ->
                val u = upperTriangularOf(m)
                eqZ(DenseMatOps.det(z, u), diagProduct(u)) shouldBe true
            }
        }

        test("swapping two rows flips the sign of the determinant") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 2, 5)) { m ->
                val swapped = DenseMat.tabulate(m.rows, m.cols) { r, c ->
                    when (r) {
                        0 -> m[1, c]
                        1 -> m[0, c]
                        else -> m[r, c]
                    }
                }
                eqZ(DenseMatOps.det(z, swapped), z.add.inverse(DenseMatOps.det(z, m))) shouldBe true
            }
        }

        test("a matrix with two equal rows is singular (det = 0)") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 2, 5)) { m ->
                val dup = DenseMat.tabulate(m.rows, m.cols) { r, c -> if (r == 1) m[0, c] else m[r, c] }
                eqZ(DenseMatOps.det(z, dup), BigInteger.ZERO) shouldBe true
            }
        }

        test("scaling a single row by k scales the determinant by k") {
            val k = BigInteger.valueOf(3)
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 1, 5)) { m ->
                val scaledRow = DenseMat.tabulate(m.rows, m.cols) { r, c ->
                    if (r == 0) k.multiply(m[r, c]) else m[r, c]
                }
                eqZ(DenseMatOps.det(z, scaledRow), k.multiply(DenseMatOps.det(z, m))) shouldBe true
            }
        }
    }

    context("permutation matrices and the Leibniz sign") {
        test("det of a permutation matrix is +1 for even and -1 for odd permutations") {
            checkAll(arbPermMatOfVaryingSize(0, 6)) { pm ->
                val expected = if (pm.isEven()) BigInteger.ONE else BigInteger.valueOf(-1)
                eqZ(DenseMatOps.det(z, permToZMat(pm)), expected) shouldBe true
            }
        }
    }

    context("API contract") {
        test("det agrees with detByPermutation") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 0, 5)) { m ->
                eqZ(DenseMatOps.det(z, m), DenseMatOps.detByPermutation(z, m)) shouldBe true
            }
        }

        test("det of a non-square matrix throws") {
            shouldThrow<IllegalArgumentException> {
                DenseMatOps.det(z, zMat(listOf(1, 2, 3), listOf(4, 5, 6)))
            }
        }

        test("detByPermutation of a non-square matrix throws") {
            shouldThrow<IllegalArgumentException> {
                DenseMatOps.detByPermutation(z, zMat(listOf(1, 2, 3), listOf(4, 5, 6)))
            }
        }
    }
})
