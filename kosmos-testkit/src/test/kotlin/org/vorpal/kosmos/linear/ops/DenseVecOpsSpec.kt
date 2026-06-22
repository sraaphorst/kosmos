package org.vorpal.kosmos.linear.ops

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import java.math.BigInteger

/**
 * Tests for the public vector operations in [DenseVecOps], which delegate to the
 * module-internal `DenseVecKernel`. Structural law suites live in `DenseVecStructuresSpec`.
 *
 * Most checks are over the integers `ℤ`; [DenseVec] and [DenseMat] both have value-based
 * `equals`, so results are compared directly against expected values.
 */
class DenseVecOpsSpec : FunSpec({

    val z = IntegerAlgebras.IntegerCommutativeRing
    val q = RationalAlgebras.RationalField

    fun v(vararg xs: Int): DenseVec<BigInteger> =
        DenseVec.of(xs.map { it.toBigInteger() })

    fun m(vararg rows: List<Int>): DenseMat<BigInteger> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toBigInteger() } })

    context("elementwise arithmetic") {
        test("hadamard, add and scale") {
            DenseVecOps.hadamard(z, v(1, 2, 3), v(4, 5, 6)) shouldBe v(4, 10, 18)
            DenseVecOps.add(z, v(1, 2), v(3, 4)) shouldBe v(4, 6)
            DenseVecOps.scale(z, 3.toBigInteger(), v(1, 2)) shouldBe v(3, 6)
        }

        test("axpy computes a·x + y") {
            DenseVecOps.axpy(z, 2.toBigInteger(), v(1, 2), v(3, 4)) shouldBe v(5, 8)
        }

        test("constant / zero / one vectors") {
            DenseVecOps.constantVec(7.toBigInteger(), 3) shouldBe v(7, 7, 7)
            DenseVecOps.zeroVec(z, 3) shouldBe v(0, 0, 0)
            DenseVecOps.oneVec(z, 3) shouldBe v(1, 1, 1)
        }
    }

    context("inner products") {
        test("dot and normSq") {
            DenseVecOps.dot(z, v(1, 2, 3), v(4, 5, 6)) shouldBe 32.toBigInteger()
            DenseVecOps.normSq(z, v(1, 2, 3)) shouldBe 14.toBigInteger()
        }

        test("conjugated dots coincide with dot over a trivially-involutive ring") {
            DenseVecOps.dotConjX(z, v(1, 2, 3), v(4, 5, 6)) shouldBe 32.toBigInteger()
            DenseVecOps.dotConjY(z, v(1, 2, 3), v(4, 5, 6)) shouldBe 32.toBigInteger()
        }
    }

    context("outer products and rank-1 updates") {
        test("outerProduct") {
            DenseVecOps.outerProduct(z, v(1, 2), v(3, 4)) shouldBe m(listOf(3, 4), listOf(6, 8))
        }

        test("outerProductConjY coincides with outerProduct over a trivially-involutive ring") {
            DenseVecOps.outerProductConjY(z, v(1, 2), v(3, 4)) shouldBe m(listOf(3, 4), listOf(6, 8))
        }

        test("rank1Update adds alpha·x·yᵀ to the base matrix") {
            DenseVecOps.rank1Update(z, BigInteger.ONE, v(1, 2), v(3, 4), DenseMatOps.zero(z, 2, 2)) shouldBe
                m(listOf(3, 4), listOf(6, 8))
            DenseVecOps.rank1UpdateConjY(z, BigInteger.ONE, v(1, 2), v(3, 4), DenseMatOps.zero(z, 2, 2)) shouldBe
                m(listOf(3, 4), listOf(6, 8))
        }
    }

    context("higher-order helpers") {
        test("map and mapIndexed") {
            DenseVecOps.map(v(1, 2, 3)) { it.multiply(2.toBigInteger()) } shouldBe v(2, 4, 6)
            DenseVecOps.mapIndexed(v(10, 10, 10)) { i, x -> x.add(i.toBigInteger()) } shouldBe v(10, 11, 12)
        }

        test("foldLeft and reduceLeft") {
            DenseVecOps.foldLeft(v(1, 2, 3), BigInteger.ZERO) { acc, x -> acc.add(x) } shouldBe 6.toBigInteger()
            DenseVecOps.reduceLeft(v(1, 2, 3)) { acc, x -> acc.add(x) } shouldBe Option.Some(6.toBigInteger())
            DenseVecOps.reduceLeft(DenseVec.of(emptyList<BigInteger>())) { a, b -> a.add(b) } shouldBe Option.None
        }

        test("sum and sumOf") {
            DenseVecOps.sum(z, v(1, 2, 3)) shouldBe 6.toBigInteger()
            DenseVecOps.sum(z.add, v(1, 2, 3)) shouldBe 6.toBigInteger()
            DenseVecOps.sumOf(z, v(1, 2, 3)) { it.multiply(2.toBigInteger()) } shouldBe 12.toBigInteger()
        }

        test("all / any / none") {
            val xs = v(1, 2, 3)
            DenseVecOps.all(xs) { it > BigInteger.ZERO } shouldBe true
            DenseVecOps.any(xs) { it > 2.toBigInteger() } shouldBe true
            DenseVecOps.none(xs) { it > 10.toBigInteger() } shouldBe true
        }
    }

    context("predicates") {
        test("isAll") {
            DenseVecOps.isAll(v(5, 5, 5), 5.toBigInteger()) shouldBe true
            DenseVecOps.isAll(v(5, 6, 5), 5.toBigInteger()) shouldBe false
        }

        test("isHadamardUnit over a field detects zeros") {
            DenseVecOps.isHadamardUnit(q, DenseVec.of(Rational.ONE, 2.toRational())) shouldBe true
            DenseVecOps.isHadamardUnit(q, DenseVec.of(Rational.ONE, Rational.ZERO)) shouldBe false
        }
    }

    context("extrema") {
        val xs = v(3, 1, 2)
        val cmp = naturalOrder<BigInteger>()

        test("argmin / argmax / min / max") {
            DenseVecOps.argmin(xs, cmp) shouldBe Option.Some(1)
            DenseVecOps.argmax(xs, cmp) shouldBe Option.Some(0)
            DenseVecOps.min(xs, cmp) shouldBe Option.Some(BigInteger.ONE)
            DenseVecOps.max(xs, cmp) shouldBe Option.Some(3.toBigInteger())
        }

        test("argminBy / argmaxBy by a key function") {
            DenseVecOps.argminBy(xs, { (it - 2.toBigInteger()).abs() }, cmp) shouldBe Option.Some(2)
            DenseVecOps.argmaxBy(xs, { (it - 2.toBigInteger()).abs() }, cmp) shouldBe Option.Some(0)
        }

        test("argmin on the empty vector is None") {
            DenseVecOps.argmin(DenseVec.of(emptyList<BigInteger>()), cmp) shouldBe Option.None
        }
    }

    context("structural helpers") {
        test("copy returns an equal vector") {
            DenseVecOps.copy(v(1, 2, 3)) shouldBe v(1, 2, 3)
        }

        test("concat joins vectors end to end") {
            DenseVecOps.concat(v(1, 2), v(3, 4, 5)) shouldBe v(1, 2, 3, 4, 5)
            DenseVecOps.concat(listOf(v(1, 2), v(3, 4, 5))) shouldBe v(1, 2, 3, 4, 5)
        }
    }
})
