package org.vorpal.kosmos.linear.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import java.math.BigInteger

/**
 * Tests for the public matrix operations in [DenseMatOps], which delegate to the
 * module-internal `DenseMatKernel`. Determinant operations are covered separately in
 * [DenseMatDeterminantSpec]; structural law suites live in `DenseMatStructuresSpec`.
 *
 * Most checks are over the integers `ℤ`. Because [DenseMat] has value-based `equals`,
 * results are compared directly against expected matrices.
 */
class DenseMatOpsSpec : FunSpec({

    val z = IntegerAlgebras.IntegerCommutativeRing
    val q = RationalAlgebras.RationalField

    fun m(vararg rows: List<Int>): DenseMat<BigInteger> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toBigInteger() } })

    fun v(vararg xs: Int): DenseVec<BigInteger> =
        DenseVec.of(xs.map { it.toBigInteger() })

    fun qm(vararg rows: List<Rational>): DenseMat<Rational> =
        DenseMat.ofRows(rows.map { it })

    context("elementwise structure") {
        test("matAdd adds entrywise") {
            DenseMatOps.matAdd(z, m(listOf(1, 2), listOf(3, 4)), m(listOf(5, 6), listOf(7, 8))) shouldBe
                m(listOf(6, 8), listOf(10, 12))
        }

        test("hadamard multiplies entrywise") {
            DenseMatOps.hadamard(z, m(listOf(1, 2), listOf(3, 4)), m(listOf(5, 6), listOf(7, 8))) shouldBe
                m(listOf(5, 12), listOf(21, 32))
        }

        test("constMat / zero / one fill all entries") {
            DenseMatOps.constMat(7.toBigInteger(), 2, 3) shouldBe m(listOf(7, 7, 7), listOf(7, 7, 7))
            DenseMatOps.zero(z, 2, 2) shouldBe m(listOf(0, 0), listOf(0, 0))
            DenseMatOps.one(z, 2, 2) shouldBe m(listOf(1, 1), listOf(1, 1))
        }

        test("isAll detects constant matrices") {
            DenseMatOps.isAll(DenseMatOps.constMat(5.toBigInteger(), 2, 2), 5.toBigInteger()) shouldBe true
            DenseMatOps.isAll(DenseMatOps.constMat(5.toBigInteger(), 2, 2), 6.toBigInteger()) shouldBe false
        }

        test("isEqual is shape- and entry-sensitive") {
            DenseMatOps.isEqual(m(listOf(1, 2)), m(listOf(1, 2))) shouldBe true
            DenseMatOps.isEqual(m(listOf(1, 2)), m(listOf(1, 3))) shouldBe false
            DenseMatOps.isEqual(m(listOf(1, 2)), m(listOf(1), listOf(2))) shouldBe false
        }

        test("copy produces an equal independent matrix") {
            val original = m(listOf(1, 2), listOf(3, 4))
            DenseMatOps.copy(original) shouldBe original
        }
    }

    context("products") {
        test("matMul matches a hand computation") {
            DenseMatOps.matMul(z, m(listOf(1, 2), listOf(3, 4)), m(listOf(5, 6), listOf(7, 8))) shouldBe
                m(listOf(19, 22), listOf(43, 50))
        }

        test("identity is a multiplicative neutral element") {
            val a = m(listOf(1, 2), listOf(3, 4))
            DenseMatOps.matMul(z, a, DenseMatOps.identity(z, 2)) shouldBe a
            DenseMatOps.matMul(z, DenseMatOps.identity(z, 2), a) shouldBe a
        }

        test("matVec matches a hand computation") {
            DenseMatOps.matVec(z, m(listOf(1, 2), listOf(3, 4)), v(5, 6)) shouldBe v(17, 39)
        }

        test("kronecker with the identity gives a block-diagonal layout") {
            DenseMatOps.kronecker(z, DenseMatOps.identity(z, 2), m(listOf(1, 2), listOf(3, 4))) shouldBe
                m(
                    listOf(1, 2, 0, 0),
                    listOf(3, 4, 0, 0),
                    listOf(0, 0, 1, 2),
                    listOf(0, 0, 3, 4),
                )
        }

        test("gramMatrix and intersectionMatrix equal the corresponding products") {
            val a = m(listOf(1, 2), listOf(3, 4))
            DenseMatOps.gramMatrix(z, a) shouldBe
                DenseMatOps.matMul(z, DenseMatOps.generateTranspose(a), a)
            DenseMatOps.intersectionMatrix(z, a) shouldBe
                DenseMatOps.matMul(z, a, DenseMatOps.generateTranspose(a))
        }

        test("power: A^0 = I, A^1 = A, A^2 = A·A") {
            val a = m(listOf(1, 1), listOf(0, 1))
            DenseMatOps.power(z, a, 0) shouldBe DenseMatOps.identity(z, 2)
            DenseMatOps.power(z, a, 1) shouldBe a
            DenseMatOps.power(z, a, 2) shouldBe DenseMatOps.matMul(z, a, a)
            DenseMatOps.power(z, a, 5) shouldBe m(listOf(1, 5), listOf(0, 1))
        }

        test("pointInflation replicates each entry into a block") {
            DenseMatOps.pointInflation(m(listOf(1, 2)), 2) shouldBe
                m(listOf(1, 1, 2, 2), listOf(1, 1, 2, 2))
        }
    }

    context("affine kernels") {
        test("affineMul computes alpha·A·B + beta·C") {
            val a = DenseMatOps.identity(z, 2)
            val b = m(listOf(1, 2), listOf(3, 4))
            val c = m(listOf(1, 1), listOf(1, 1))
            DenseMatOps.affineMul(
                z, 2.toBigInteger(), MatOp.Normal, a, MatOp.Normal, b, 3.toBigInteger(), c
            ) shouldBe m(listOf(5, 7), listOf(9, 11))
        }

        test("affineMatVec computes alpha·A·x + beta·y") {
            val a = DenseMatOps.identity(z, 2)
            DenseMatOps.affineMatVec(
                z, 2.toBigInteger(), MatOp.Normal, a, v(1, 2), 3.toBigInteger(), v(1, 1)
            ) shouldBe v(5, 7)
        }

        test("affineMul with a transposed operand uses the transpose") {
            val a = m(listOf(1, 2), listOf(3, 4))
            DenseMatOps.affineMul(
                z, BigInteger.ONE, MatOp.Trans, a, MatOp.Normal, DenseMatOps.identity(z, 2),
                BigInteger.ZERO, DenseMatOps.zero(z, 2, 2)
            ) shouldBe DenseMatOps.generateTranspose(a)
        }
    }

    context("reductions and diagonals") {
        test("trace sums the main diagonal") {
            DenseMatOps.trace(z, m(listOf(1, 2), listOf(3, 4))) shouldBe 5.toBigInteger()
        }

        test("traceRect sums the leading diagonal of a rectangular matrix") {
            DenseMatOps.traceRect(z, m(listOf(1, 2, 3), listOf(4, 5, 6))) shouldBe 6.toBigInteger()
        }

        test("rowSums and colSums") {
            DenseMatOps.rowSums(z, m(listOf(1, 2, 3), listOf(4, 5, 6))) shouldBe v(6, 15)
            DenseMatOps.colSums(z, m(listOf(1, 2, 3), listOf(4, 5, 6))) shouldBe v(5, 7, 9)
        }

        test("rowReduce and colReduce reduce with a monoid") {
            DenseMatOps.rowReduce(z.add, m(listOf(1, 2, 3), listOf(4, 5, 6))) { it } shouldBe v(6, 15)
            DenseMatOps.colReduce(z.add, m(listOf(1, 2, 3), listOf(4, 5, 6))) { it } shouldBe v(5, 7, 9)
        }

        test("extractDiagonal / fromDiagonal round-trip") {
            DenseMatOps.extractDiagonal(m(listOf(1, 9), listOf(8, 2))) shouldBe v(1, 2)
            DenseMatOps.fromDiagonal(BigInteger.ZERO, v(1, 2, 3)) shouldBe
                m(listOf(1, 0, 0), listOf(0, 2, 0), listOf(0, 0, 3))
            DenseMatOps.fromDiagonal(BigInteger.ZERO, 2) { (it + 1).toBigInteger() } shouldBe
                m(listOf(1, 0), listOf(0, 2))
        }

        test("generateTranspose swaps axes and is involutive") {
            val a = m(listOf(1, 2, 3), listOf(4, 5, 6))
            DenseMatOps.generateTranspose(a) shouldBe m(listOf(1, 4), listOf(2, 5), listOf(3, 6))
            DenseMatOps.generateTranspose(DenseMatOps.generateTranspose(a)) shouldBe a
        }

        test("concatDiagonal places matrices on the diagonal") {
            DenseMatOps.concatDiagonal(
                BigInteger.ZERO,
                m(listOf(1, 2, 3), listOf(4, 5, 6)),
                m(listOf(7), listOf(8)),
                m(listOf(9, 10)),
            ) shouldBe m(
                listOf(1, 2, 3, 0, 0, 0),
                listOf(4, 5, 6, 0, 0, 0),
                listOf(0, 0, 0, 7, 0, 0),
                listOf(0, 0, 0, 8, 0, 0),
                listOf(0, 0, 0, 0, 9, 10),
            )
        }
    }

    context("predicates") {
        test("isDiagonal") {
            DenseMatOps.isDiagonal(m(listOf(1, 0), listOf(0, 2)), BigInteger.ZERO) shouldBe true
            DenseMatOps.isDiagonal(m(listOf(1, 1), listOf(0, 2)), BigInteger.ZERO) shouldBe false
        }

        test("isLowerTriangular and isUpperTriangular") {
            DenseMatOps.isLowerTriangular(m(listOf(1, 0), listOf(2, 3)), BigInteger.ZERO) shouldBe true
            DenseMatOps.isUpperTriangular(m(listOf(1, 2), listOf(0, 3)), BigInteger.ZERO) shouldBe true
            DenseMatOps.isUpperTriangular(m(listOf(1, 2), listOf(4, 3)), BigInteger.ZERO) shouldBe false
        }

        test("isSymmetric") {
            DenseMatOps.isSymmetric(m(listOf(1, 2), listOf(2, 1))) shouldBe true
            DenseMatOps.isSymmetric(m(listOf(1, 2), listOf(3, 1))) shouldBe false
        }

        test("isPermutationMatrix") {
            DenseMatOps.isPermutationMatrix(DenseMatOps.identity(z, 3), BigInteger.ZERO, BigInteger.ONE) shouldBe true
            DenseMatOps.isPermutationMatrix(z, m(listOf(0, 1), listOf(1, 0))) shouldBe true
            DenseMatOps.isPermutationMatrix(z, m(listOf(1, 1), listOf(0, 0))) shouldBe false
        }

        test("isHadamardUnit over a field detects zero entries") {
            DenseMatOps.isHadamardUnit(q, qm(listOf(Rational.ONE, 2.toRational()))) shouldBe true
            DenseMatOps.isHadamardUnit(q, qm(listOf(Rational.ONE, Rational.ZERO))) shouldBe false
        }

        test("diagonal dominance") {
            val mag = { x: BigInteger -> x.abs() }
            val order = TotalOrder.naturalOrder<BigInteger>()
            val dominant = m(listOf(10, 1, 2), listOf(1, 10, 3), listOf(2, 3, 10))
            val notDominant = m(listOf(1, 2), listOf(3, 1))

            DenseMatOps.isRowDiagonallyDominant(dominant, mag, z.add, order) shouldBe true
            DenseMatOps.isRowDiagonallyDominantStrict(dominant, mag, z.add, order) shouldBe true
            DenseMatOps.isRowDiagonallyDominant(notDominant, mag, z.add, order) shouldBe false
            DenseMatOps.isColDiagonallyDominant(dominant, mag, z.add, order) shouldBe true
            DenseMatOps.isColDiagonallyDominantStrict(dominant, mag, z.add, order) shouldBe true
        }
    }

    context("argmin / argmax") {
        val grid = m(listOf(3, 1, 4), listOf(1, 5, 9))
        val cmp = naturalOrder<BigInteger>()

        test("argmin / argmax / min / max on a populated matrix") {
            DenseMatOps.argmin(grid, cmp) shouldBe Option.Some(0 to 1)
            DenseMatOps.argmax(grid, cmp) shouldBe Option.Some(1 to 2)
            DenseMatOps.min(grid, cmp) shouldBe Option.Some(BigInteger.ONE)
            DenseMatOps.max(grid, cmp) shouldBe Option.Some(9.toBigInteger())
        }

        test("argminBy / argmaxBy with a key function") {
            // Key by distance from 5: minimum key at value 4 or 5.
            DenseMatOps.argmaxBy(grid, { (it - 5.toBigInteger()).abs() }, cmp) shouldBe Option.Some(0 to 1)
        }

        test("argmin on an empty matrix is None") {
            DenseMatOps.argmin(DenseMat.tabulate(0, 0) { _, _ -> BigInteger.ZERO }, cmp) shouldBe Option.None
        }
    }

    context("shape requirements") {
        test("matMul with incompatible shapes throws") {
            shouldThrow<IllegalArgumentException> {
                DenseMatOps.matMul(z, m(listOf(1, 2)), m(listOf(1, 2)))
            }
        }
    }
})
