package org.vorpal.kosmos.linear.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.arbMat2
import org.vorpal.kosmos.linear.values.arbVec2
import org.vorpal.kosmos.linear.views.asRowMatView
import org.vorpal.kosmos.linear.views.transposeView
import java.math.BigInteger

/**
 * The closed 2 × 2 formulas in [FixedMatOps], checked two ways:
 * - against the general [DenseMatOps] (and the views), which serve as independent oracles; and
 * - against the identities of `M₂(R)` over a commutative ring: Cayley-Hamilton, multiplicativity
 *   of the determinant, and the adjugate as an involutive anti-automorphism with
 *   `M · adj(M) = det(M) · I`.
 *
 * Exact throughout: over ℤ, over 𝔽₅, and over the rational quaternions where the entries do not
 * commute.
 */
class FixedMatOpsSpec : StringSpec({
    val z = IntegerAlgebras.IntegerCommutativeRing
    val arbZ = Arb.int(-20..20).map(Int::toBigInteger)
    val arbM = arbMat2(arbZ)
    val arbV = arbVec2(arbZ)

    val f5 = PrimeField(BigInteger.valueOf(5))
    val arbF5 = Arb.int(0..4).map(Int::toBigInteger)
    val arbMF5 = arbMat2(arbF5)

    fun <A : Any> dense(m: Mat2<A>): DenseMat<A> =
        DenseMat.ofRows(listOf(listOf(m.a, m.b), listOf(m.c, m.d)))

    fun <A : Any> dense(v: Vec2<A>): DenseVec<A> =
        DenseVec.of(v.x, v.y)

    fun mul(m: Mat2<BigInteger>, n: Mat2<BigInteger>) = FixedMatOps.matMul(z, m, n)
    fun adj(m: Mat2<BigInteger>) = FixedMatOps.adjugate(z, m)
    fun det(m: Mat2<BigInteger>) = FixedMatOps.det(z, m)
    fun tr(m: Mat2<BigInteger>) = FixedMatOps.trace(z, m)
    fun scale(s: BigInteger, m: Mat2<BigInteger>) = FixedMatOps.scale(z, s, m)
    fun add(m: Mat2<BigInteger>, n: Mat2<BigInteger>) = FixedMatOps.matAdd(z, m, n)
    val identity = FixedMatOps.identity(z)
    val zero = FixedMatOps.zero(z)

    // ---------- Mat2 as a MatLike ----------

    "Mat2 is a 2 × 2 MatLike read row by row, and rejects other positions" {
        val m = Mat2(1, 2, 3, 4)
        m.rows shouldBe 2
        m.cols shouldBe 2
        listOf(m[0, 0], m[0, 1], m[1, 0], m[1, 1]) shouldBe listOf(1, 2, 3, 4)
        val (a, b, c, d) = m
        listOf(a, b, c, d) shouldBe listOf(1, 2, 3, 4)
        shouldThrow<IndexOutOfBoundsException> { m[2, 0] }
        shouldThrow<IndexOutOfBoundsException> { m[0, 2] }
        shouldThrow<IndexOutOfBoundsException> { m[-1, 1] }
    }

    // ---------- Oracles: DenseMatOps and the views ----------

    "matMul, matAdd and scale agree with DenseMatOps" {
        checkAll(arbM, arbM, arbZ) { m, n, s ->
            dense(mul(m, n)) shouldBe DenseMatOps.matMul(z, m, n)
            dense(add(m, n)) shouldBe DenseMatOps.matAdd(z, m, n)
            dense(scale(s, m)) shouldBe DenseMatOps.scale(z, s, m)
            dense(FixedMatOps.matNegate(z, m)) shouldBe DenseMatOps.scale(z, BigInteger.ONE.negate(), m)
        }
    }

    "identity and zero agree with DenseMatOps" {
        dense(identity) shouldBe DenseMatOps.identity(z, 2)
        dense(zero) shouldBe DenseMatOps.zero(z, 2, 2)
    }

    "det and trace agree with DenseMatOps (Leibniz and the general trace)" {
        checkAll(arbM) { m ->
            det(m) shouldBe DenseMatOps.det(z, m)
            det(m) shouldBe DenseMatOps.detByPermutation(z, m)
            tr(m) shouldBe DenseMatOps.trace(z, m)
        }
    }

    "matVec agrees with DenseMatOps.matVec, and vecMat with a row view times the matrix" {
        checkAll(arbM, arbV) { m, v ->
            dense(FixedMatOps.matVec(z, m, v)) shouldBe DenseMatOps.matVec(z, m, v)
            val row = DenseMatOps.matMul(z, v.asRowMatView(), m)
            val vm = FixedMatOps.vecMat(z, v, m)
            listOf(vm.x, vm.y) shouldBe listOf(row[0, 0], row[0, 1])
        }
    }

    "transpose agrees with transposeView and generateTranspose" {
        checkAll(arbM) { m ->
            val t = FixedMatOps.transpose(m)
            DenseMatOps.isEqual(t, m.transposeView()) shouldBe true
            dense(t) shouldBe DenseMatOps.generateTranspose(m)
        }
    }

    "with non-commuting entries (rational quaternions) the products keep the order of the factors" {
        val h = QuaternionAlgebras.quaternionRing(RationalAlgebras.RationalField)
        val arbH = ArbRationalQuaternion.smallRationalQuaternion
        val arbHM = arbMat2(arbH)
        checkAll(arbHM, arbHM, arbVec2(arbH)) { m, n, v ->
            dense(FixedMatOps.matMul(h, m, n)) shouldBe DenseMatOps.matMul(h, m, n)
            dense(FixedMatOps.matVec(h, m, v)) shouldBe DenseMatOps.matVec(h, m, v)
            val row = DenseMatOps.matMul(h, v.asRowMatView(), m)
            val vm = FixedMatOps.vecMat(h, v, m)
            listOf(vm.x, vm.y) shouldBe listOf(row[0, 0], row[0, 1])
        }
    }

    // ---------- Identities over a commutative ring ----------

    "Cayley-Hamilton: M² = tr(M)·M − det(M)·I" {
        checkAll(arbM) { m ->
            mul(m, m) shouldBe add(scale(tr(m), m), scale(det(m).negate(), identity))
        }
    }

    "the determinant is multiplicative and the trace is cyclic" {
        checkAll(arbM, arbM) { m, n ->
            det(mul(m, n)) shouldBe det(m) * det(n)
            tr(mul(m, n)) shouldBe tr(mul(n, m))
        }
    }

    "M · adj(M) = adj(M) · M = det(M)·I, and M + adj(M) = tr(M)·I" {
        checkAll(arbM) { m ->
            mul(m, adj(m)) shouldBe scale(det(m), identity)
            mul(adj(m), m) shouldBe scale(det(m), identity)
            add(m, adj(m)) shouldBe scale(tr(m), identity)
        }
    }

    "the adjugate is an involutive anti-automorphism: adj(MN) = adj(N)·adj(M), adj(adj(M)) = M" {
        checkAll(arbM, arbM) { m, n ->
            adj(mul(m, n)) shouldBe mul(adj(n), adj(m))
            adj(adj(m)) shouldBe m
            adj(add(m, n)) shouldBe add(adj(m), adj(n))
        }
        adj(identity) shouldBe identity
    }

    "the transpose reverses products: (MN)ᵀ = NᵀMᵀ" {
        checkAll(arbM, arbM) { m, n ->
            FixedMatOps.transpose(mul(m, n)) shouldBe mul(FixedMatOps.transpose(n), FixedMatOps.transpose(m))
        }
    }

    "columns are a left module and rows a right module: (MN)v = M(Nv), v(MN) = (vM)N" {
        checkAll(arbM, arbM, arbV) { m, n, v ->
            FixedMatOps.matVec(z, mul(m, n), v) shouldBe FixedMatOps.matVec(z, m, FixedMatOps.matVec(z, n, v))
            FixedMatOps.vecMat(z, v, mul(m, n)) shouldBe FixedMatOps.vecMat(z, FixedMatOps.vecMat(z, v, m), n)
        }
    }

    "the matrix product is not commutative: E₁₂E₂₁ = E₁₁ but E₂₁E₁₂ = E₂₂" {
        val (o, l) = BigInteger.ZERO to BigInteger.ONE
        val e12 = Mat2(o, l, o, o)
        val e21 = Mat2(o, o, l, o)
        mul(e12, e21) shouldBe Mat2(l, o, o, o)
        mul(e21, e12) shouldBe Mat2(o, o, o, l)
        mul(e12, e21) shouldNotBe mul(e21, e12)
    }

    "over 𝔽₅: Cayley-Hamilton, det multiplicative, M · adj(M) = det(M)·I" {
        fun mulF(m: Mat2<BigInteger>, n: Mat2<BigInteger>) = FixedMatOps.matMul(f5, m, n)
        val idF = FixedMatOps.identity(f5)
        checkAll(arbMF5, arbMF5) { m, n ->
            val detM = FixedMatOps.det(f5, m)
            val trM = FixedMatOps.trace(f5, m)
            mulF(m, m) shouldBe FixedMatOps.matAdd(
                f5,
                FixedMatOps.scale(f5, trM, m),
                FixedMatOps.scale(f5, f5.add.inverse(detM), idF)
            )
            FixedMatOps.det(f5, mulF(m, n)) shouldBe f5.mul(detM, FixedMatOps.det(f5, n))
            mulF(m, FixedMatOps.adjugate(f5, m)) shouldBe FixedMatOps.scale(f5, detM, idF)
            FixedMatOps.adjugate(f5, mulF(m, n)) shouldBe
                mulF(FixedMatOps.adjugate(f5, n), FixedMatOps.adjugate(f5, m))
        }
    }
})
