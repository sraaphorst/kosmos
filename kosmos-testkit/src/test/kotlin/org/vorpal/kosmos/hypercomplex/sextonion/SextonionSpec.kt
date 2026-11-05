package org.vorpal.kosmos.hypercomplex.sextonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.linear.ops.FixedMatOps
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import java.math.BigInteger

/**
 * Exact tests of the sextonion ring `(A, ν)(B, μ) = (AB, μA + ν·adj(B))`.
 *
 * Over ℚ, ℤ and 𝔽₅: the ring laws with conjugation, alternativity, flexibility and the Moufang
 * identities, and a multiplicative norm `det A`. Then the structure that makes the sextonions
 * what they are: not associative (not even the copy of `M₂`, whose elements other than the
 * scalars all fail to lie in the nucleus), a square-zero ideal `V` on which the norm vanishes,
 * and units exactly where `det A` is invertible.
 */
class SextonionSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = SextonionAlgebras.sextonionRing(q)
    val normSq = SextonionAlgebras.normSq(q)
    val arb = ArbSextonion.rationalSextonion

    val (zero, one) = Rational.ZERO to Rational.ONE
    val zeroVec = Vec2(zero, zero)
    val zeroMat = Mat2(zero, zero, zero, zero)
    val identity = Mat2(one, zero, zero, one)

    fun scalar(r: Rational) = Sextonion(Mat2(r, zero, zero, r), zeroVec)
    fun mat(a: Int, b: Int, c: Int, d: Int) =
        Sextonion(Mat2(Rational.of(a), Rational.of(b), Rational.of(c), Rational.of(d)), zeroVec)
    fun associator(x: Sextonion<Rational>, y: Sextonion<Rational>, z: Sextonion<Rational>) =
        ring.add(ring.mul(ring.mul(x, y), z), ring.add.inverse(ring.mul(x, ring.mul(y, z))))

    val (e11, e12, e21, e22, n1, n2) = SextonionAlgebras.basis(q).let { b ->
        Sextuple(b[0], b[1], b[2], b[3], b[4], b[5])
    }

    // ---------- The laws ----------

    "the sextonions over ℚ satisfy NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(ring, arb).fullTest().throwIfFailed()
    }

    "the sextonions over ℤ and 𝔽₅ satisfy NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(
            SextonionAlgebras.sextonionRing(IntegerAlgebras.IntegerCommutativeRing),
            ArbSextonion.integerSextonion
        ).fullTest().throwIfFailed()
        val f5 = PrimeField(BigInteger.valueOf(5))
        NonAssociativeInvolutiveRingLaws(
            SextonionAlgebras.sextonionRing(f5),
            ArbSextonion.sextonionOf(Arb.int(0..4).map(Int::toBigInteger))
        ).fullTest().throwIfFailed()
    }

    "the identity is (I, 0), on both sides" {
        ring.one shouldBe Sextonion(identity, zeroVec)
        checkAll(arb) { x ->
            ring.mul(ring.one, x) shouldBe x
            ring.mul(x, ring.one) shouldBe x
        }
    }

    "the product is (AB, μA + ν·adj(B))" {
        checkAll(arb, arb) { x, y ->
            val (a, nu) = x
            val (b, mu) = y
            val va = FixedMatOps.vecMat(q, mu, a)
            val vb = FixedMatOps.vecMat(q, nu, FixedMatOps.adjugate(q, b))
            ring.mul(x, y) shouldBe Sextonion(FixedMatOps.matMul(q, a, b), Vec2(va.x + vb.x, va.y + vb.y))
        }
    }

    "sextonion multiplication is exactly alternative over ℚ and ℤ" {
        AlternativityLaw(ring.mul.op, arb).test()
        val z = SextonionAlgebras.sextonionRing(IntegerAlgebras.IntegerCommutativeRing)
        AlternativityLaw(z.mul.op, ArbSextonion.integerSextonion).test()
    }

    "sextonion multiplication is exactly flexible and Moufang" {
        FlexibilityLaw(ring.mul.op, arb).test()
        MoufangLaw(ring.mul.op, arb).test()
    }

    // ---------- Not associative, not commutative ----------

    "the sextonions are not associative: ((E₁₁, 0), (E₁₂, 0), (I, n₁)) = (0, n₂)" {
        val y = Sextonion(identity, Vec2(one, zero))
        associator(e11, e12, y) shouldBe n2
    }

    "even the matrix part leaves the nucleus: ((A, 0), (B, b), (C, c)) = (0, c[A, B] + b[A, adj C])" {
        checkAll(arb, arb, arb) { a0, x, y ->
            val a = a0.m
            val (b, bv) = x
            val (c, cv) = y
            fun comm(p: Mat2<Rational>, r: Mat2<Rational>) = FixedMatOps.matAdd(
                q, FixedMatOps.matMul(q, p, r), FixedMatOps.matNegate(q, FixedMatOps.matMul(q, r, p))
            )
            val first = FixedMatOps.vecMat(q, cv, comm(a, b))
            val second = FixedMatOps.vecMat(q, bv, comm(a, FixedMatOps.adjugate(q, c)))
            associator(Sextonion(a, zeroVec), x, y) shouldBe
                Sextonion(zeroMat, Vec2(first.x + second.x, first.y + second.y))
        }
    }

    "the scalars (rI, 0) lie in the nucleus and the center" {
        checkAll(ArbSplitQuaternion.smallRational, arb, arb) { s, x, y ->
            val r = scalar(s)
            associator(r, x, y) shouldBe ring.zero
            associator(x, r, y) shouldBe ring.zero
            associator(x, y, r) shouldBe ring.zero
            ring.mul(r, x) shouldBe ring.mul(x, r)
        }
    }

    "the sextonions are not commutative: E₁₂E₂₁ = E₁₁ but E₂₁E₁₂ = E₂₂" {
        ring.mul(e12, e21) shouldBe e11
        ring.mul(e21, e12) shouldBe e22
    }

    // ---------- Conjugation and norm ----------

    "conjugation is (A, ν) ↦ (adj A, −ν), an involutive anti-automorphism" {
        checkAll(arb, arb) { x, y ->
            val (a, nu) = x
            ring.conj(x) shouldBe Sextonion(FixedMatOps.adjugate(q, a), Vec2(-nu.x, -nu.y))
            ring.conj(ring.conj(x)) shouldBe x
            ring.conj(ring.mul(x, y)) shouldBe ring.mul(ring.conj(y), ring.conj(x))
        }
    }

    "the norm is det A, and x·conj(x) = conj(x)·x = N(x)·1" {
        checkAll(arb) { x ->
            normSq(x) shouldBe FixedMatOps.det(q, x.m)
            ring.mul(x, ring.conj(x)) shouldBe scalar(normSq(x))
            ring.mul(ring.conj(x), x) shouldBe scalar(normSq(x))
        }
    }

    "the norm is exactly multiplicative" {
        NormSqMultiplicativeLaw(mulA = ring.mul.op, normSq = normSq, mulN = q.mul.op, arbA = arb).test()
    }

    "the norm is degenerate: N(A, ν) = N(A, 0), so it vanishes on V = {(0, ν)}" {
        checkAll(arb) { x ->
            normSq(x) shouldBe normSq(Sextonion(x.m, zeroVec))
            normSq(Sextonion(zeroMat, x.nu)) shouldBe zero
        }
    }

    // ---------- The ideal V and the quotient M₂ ----------

    "V = {(0, ν)} is an ideal with V·V = 0" {
        checkAll(arb, arb, arb) { x, u0, w0 ->
            val u = Sextonion(zeroMat, u0.nu)
            val w = Sextonion(zeroMat, w0.nu)
            ring.mul(u, w) shouldBe ring.zero
            ring.mul(x, u).m shouldBe zeroMat
            ring.mul(u, x).m shouldBe zeroMat
        }
    }

    "S/V ≅ M₂: (A, ν) ↦ A is multiplicative" {
        checkAll(arb, arb) { x, y ->
            ring.mul(x, y).m shouldBe FixedMatOps.matMul(q, x.m, y.m)
        }
    }

    "the basis: E₁₁, E₂₂ are orthogonal idempotents summing to 1, and n₁, n₂ square to zero" {
        ring.mul(e11, e11) shouldBe e11
        ring.mul(e22, e22) shouldBe e22
        ring.mul(e11, e22) shouldBe ring.zero
        ring.mul(e22, e11) shouldBe ring.zero
        ring.add(e11, e22) shouldBe ring.one
        listOf(n1, n2).forEach { n -> listOf(n1, n2).forEach { m -> ring.mul(n, m) shouldBe ring.zero } }
        listOf(e11, e12, e21, e22, n1, n2).map { it.coefficients() } shouldBe
            (0 until 6).map { i -> (0 until 6).map { j -> if (i == j) one else zero } }
        checkAll(arb) { x ->
            val coefficients = x.coefficients()
            val sum = listOf(e11, e12, e21, e22, n1, n2).zip(coefficients)
                .map { (e, c) -> SextonionAlgebras.sextonionLeftAction(q)(c, e) }
                .fold(ring.zero, ring.add::invoke)
            sum shouldBe x
        }
    }

    // ---------- Units ----------

    "units over ℚ: x⁻¹ = conj(x)/N(x) = (A⁻¹, −ν/det A) is two-sided, and x⁻¹(xy) = y = (yx)x⁻¹" {
        val units = SextonionAlgebras.sextonionRingWithReciprocal(q)
        checkAll(ArbSextonion.unitRationalSextonion, arb) { x, y ->
            val xInv = units.reciprocal(x)
            val d = FixedMatOps.det(q, x.m)
            xInv shouldBe Sextonion(
                FixedMatOps.scale(q, d.reciprocal(), FixedMatOps.adjugate(q, x.m)),
                Vec2(-x.nu.x / d, -x.nu.y / d)
            )
            ring.mul(x, xInv) shouldBe ring.one
            ring.mul(xInv, x) shouldBe ring.one
            ring.mul(xInv, ring.mul(x, y)) shouldBe y
            ring.mul(ring.mul(y, x), xInv) shouldBe y
        }
    }

    "x is a unit iff det A ≠ 0: (I, ν) is a unit for every ν, and (0, ν) never is" {
        val units = SextonionAlgebras.sextonionRingWithReciprocal(q)
        checkAll(arb) { x ->
            units.hasReciprocal(Sextonion(identity, x.nu)) shouldBe true
            units.hasReciprocal(Sextonion(zeroMat, x.nu)) shouldBe false
        }
        shouldThrow<ArithmeticException> { units.reciprocal(n1) }
    }

    "over 𝔽₃ exactly |GL₂(𝔽₃)|·3² = 48·9 = 432 of the 729 sextonions are units" {
        val f3 = PrimeField(BigInteger.valueOf(3))
        val units = SextonionAlgebras.sextonionRingWithReciprocal(f3)
        val elements = (0..2).map(Int::toBigInteger)
        val all = elements.flatMap { a -> elements.flatMap { b -> elements.flatMap { c ->
            elements.flatMap { d -> elements.flatMap { e -> elements.map { f ->
                Sextonion(Mat2(a, b, c, d), Vec2(e, f))
            } } } } } }
        all.size shouldBe 729
        val invertible = all.filter(units::hasReciprocal)
        invertible.size shouldBe 432
        invertible.forEach { x ->
            units.mul(x, units.reciprocal(x)) shouldBe units.one
            units.mul(units.reciprocal(x), x) shouldBe units.one
        }
    }
})

private data class Sextuple<A>(val a: A, val b: A, val c: A, val d: A, val e: A, val f: A)
