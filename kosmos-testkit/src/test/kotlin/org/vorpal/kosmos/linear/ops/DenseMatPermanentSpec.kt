package org.vorpal.kosmos.linear.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.linear.instance.arbDenseVecOfVaryingSize
import org.vorpal.kosmos.linear.instance.arbSquareDenseMat
import org.vorpal.kosmos.linear.instance.arbSquareDenseMatOfVaryingSize
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.views.transposeView
import java.math.BigInteger

/**
 * Tests for [DenseMatOps.perm] and [DenseMatOps.permByPermutation] (the matrix permanent),
 * which delegate to the module-internal `DenseMatKernel.permByPermutation` over a [org.vorpal.kosmos.algebra.structures.Semiring].
 *
 * The permanent is the unsigned analogue of the determinant: it sums `∏ᵢ mat[i, σ(i)]` over all
 * permutations `σ` with no sign. For a 0/1 matrix it counts perfect matchings of the associated
 * bipartite graph (equivalently, the permutations choosing exactly one 1 from every row and column).
 */
class DenseMatPermanentSpec : FunSpec({

    // IntegerCommutativeRing is, in particular, a Semiring, which is all the permanent needs.
    val z = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger

    fun m(vararg rows: List<Int>): DenseMat<BigInteger> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toBigInteger() } })

    fun factorial(n: Int): BigInteger {
        var f = BigInteger.ONE
        var k = 2
        while (k <= n) {
            f = f.multiply(k.toBigInteger())
            k += 1
        }
        return f
    }

    fun diagProduct(mat: DenseMat<BigInteger>): BigInteger {
        var p = BigInteger.ONE
        var i = 0
        while (i < mat.rows) {
            p = p.multiply(mat[i, i])
            i += 1
        }
        return p
    }

    context("defining formula and small explicit values") {
        test("perm([[a,b],[c,d]]) = ad + bc") {
            checkAll(arbSquareDenseMat(ArbInteger.small, 2)) { mat ->
                val expected = z.add(z.mul(mat[0, 0], mat[1, 1]), z.mul(mat[0, 1], mat[1, 0]))
                eqZ(DenseMatOps.perm(z, mat), expected) shouldBe true
            }
        }

        test("a 2×2 explicit value") {
            DenseMatOps.perm(z, m(listOf(1, 2), listOf(3, 4))) shouldBe BigInteger.valueOf(10)
        }

        test("a 3×3 explicit value") {
            DenseMatOps.perm(z, m(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))) shouldBe
                BigInteger.valueOf(450)
        }
    }

    context("structured matrices") {
        test("perm(Iₙ) = 1 at every order") {
            (0..6).forEach { n ->
                eqZ(DenseMatOps.perm(z, DenseMatOps.identity(z, n)), BigInteger.ONE) shouldBe true
            }
        }

        test("perm(0ₙ) = 0 for n > 0") {
            (1..6).forEach { n ->
                eqZ(DenseMatOps.perm(z, DenseMatOps.zero(z, n, n)), BigInteger.ZERO) shouldBe true
            }
        }

        test("perm(diag(d₁,…,dₙ)) = d₁⋯dₙ") {
            checkAll(arbDenseVecOfVaryingSize(ArbInteger.small, 1, 6)) { d ->
                val mat = DenseMatOps.fromDiagonal(BigInteger.ZERO, d)
                eqZ(DenseMatOps.perm(z, mat), diagProduct(mat)) shouldBe true
            }
        }

        test("perm(Jₙ) = n! (the all-ones matrix counts all permutations)") {
            (0..6).forEach { n ->
                val ones = DenseMatOps.one(z, n, n)
                eqZ(DenseMatOps.perm(z, ones), factorial(n)) shouldBe true
            }
        }
    }

    context("0/1 matrices count perfect matchings") {
        test("[[1,1],[1,0]] has permanent 1") {
            DenseMatOps.perm(z, m(listOf(1, 1), listOf(1, 0))) shouldBe BigInteger.ONE
        }

        test("a 3×3 bipartite graph with three perfect matchings") {
            DenseMatOps.perm(z, m(listOf(1, 1, 0), listOf(1, 1, 1), listOf(0, 1, 1))) shouldBe
                BigInteger.valueOf(3)
        }
    }

    context("structural properties") {
        test("the permanent is invariant under transpose") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 0, 5)) { mat ->
                eqZ(DenseMatOps.perm(z, mat), DenseMatOps.perm(z, mat.transposeView())) shouldBe true
            }
        }

        test("perm agrees with permByPermutation") {
            checkAll(arbSquareDenseMatOfVaryingSize(ArbInteger.small, 0, 5)) { mat ->
                eqZ(DenseMatOps.perm(z, mat), DenseMatOps.permByPermutation(z, mat)) shouldBe true
            }
        }
    }

    context("API contract") {
        test("perm of a non-square matrix throws") {
            shouldThrow<IllegalArgumentException> {
                DenseMatOps.perm(z, m(listOf(1, 2, 3), listOf(4, 5, 6)))
            }
        }
    }
})
