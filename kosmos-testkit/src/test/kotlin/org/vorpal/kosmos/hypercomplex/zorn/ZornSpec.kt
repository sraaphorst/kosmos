package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.linear.values.Vec3

/**
 * Exact tests of the Zorn vector-matrix ring over ℚ.
 *
 * The product is written directly with dot and cross products, so these checks are independent of
 * the Cayley-Dickson construction behind the split-octonions.
 */
class ZornSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = ZornAlgebras.zornRing(q)
    val normSq = ZornAlgebras.normSq(q)
    val arb = ArbZorn.rationalZorn

    val zero = Rational.ZERO
    val one = Rational.ONE
    val zeroVec = Vec3(zero, zero, zero)
    val units = listOf(Vec3(one, zero, zero), Vec3(zero, one, zero), Vec3(zero, zero, one))

    fun scalar(r: Rational) = Zorn(r, zeroVec, zeroVec, r)
    fun upper(i: Int) = Zorn(zero, units[i], zeroVec, zero)
    fun lower(i: Int) = Zorn(zero, zeroVec, units[i], zero)

    "the Zorn matrices over ℚ satisfy NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(ring, arb).fullTest().throwIfFailed()
    }

    "the identity is (1, 0, 0, 1), on both sides" {
        ring.one shouldBe Zorn(one, zeroVec, zeroVec, one)
        checkAll(arb) { x ->
            ring.mul(ring.one, x) shouldBe x
            ring.mul(x, ring.one) shouldBe x
        }
    }

    "Zorn multiplication is exactly alternative: (xx)y = x(xy) and (yx)x = y(xx)" {
        AlternativityLaw(ring.mul.op, arb).test()
        checkAll(arb, arb) { x, y ->
            ring.mul(ring.mul(x, x), y) shouldBe ring.mul(x, ring.mul(x, y))
            ring.mul(ring.mul(y, x), x) shouldBe ring.mul(y, ring.mul(x, x))
        }
    }

    "Zorn multiplication is exactly flexible" {
        FlexibilityLaw(ring.mul.op, arb).test()
    }

    "Zorn multiplication exactly satisfies the Moufang identities" {
        MoufangLaw(ring.mul.op, arb).test()
    }

    "Zorn multiplication is not associative: (u₁u₂)u₃ ≠ u₁(u₂u₃) for the upper units" {
        val (u1, u2, u3) = listOf(0, 1, 2).map(::upper)
        val left = ring.mul(ring.mul(u1, u2), u3)
        val right = ring.mul(u1, ring.mul(u2, u3))
        left shouldBe Zorn(zero, zeroVec, zeroVec, -one)
        right shouldBe Zorn(-one, zeroVec, zeroVec, zero)
    }

    "Zorn multiplication is not commutative: u₁u₂ = −l₃ but u₂u₁ = l₃" {
        val (u1, u2) = upper(0) to upper(1)
        ring.mul(u1, u2) shouldBe ring.add.inverse(lower(2))
        ring.mul(u2, u1) shouldBe lower(2)
    }

    "conjugation is (a, v, w, d) ↦ (d, −v, −w, a)" {
        checkAll(arb) { x ->
            val (a, v, w, d) = x
            ring.conj(x) shouldBe Zorn(d, Vec3(-v.x, -v.y, -v.z), Vec3(-w.x, -w.y, -w.z), a)
        }
    }

    "conjugation is an involutive anti-automorphism" {
        checkAll(arb, arb) { x, y ->
            ring.conj(ring.conj(x)) shouldBe x
            ring.conj(ring.mul(x, y)) shouldBe ring.mul(ring.conj(y), ring.conj(x))
        }
    }

    "normSq formula: N(a, v, w, d) = ad − v·w" {
        checkAll(arb) { x ->
            val (a, v, w, d) = x
            normSq(x) shouldBe a * d - (v.x * w.x + v.y * w.y + v.z * w.z)
        }
    }

    "the norm is the scalar X·conj(X) = conj(X)·X" {
        checkAll(arb) { x ->
            ring.mul(x, ring.conj(x)) shouldBe scalar(normSq(x))
            ring.mul(ring.conj(x), x) shouldBe scalar(normSq(x))
        }
    }

    "normSq is exactly multiplicative: the Zorn matrices are a composition algebra" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = normSq,
            mulN = q.mul.op,
            arbA = arb
        ).test()
    }

    "units: X⁻¹ = conj(X)/N(X) is a two-sided inverse, and x⁻¹(xy) = y = (yx)x⁻¹" {
        fun inverse(x: Zorn<Rational>): Zorn<Rational> {
            val r = normSq(x).reciprocal()
            val (a, v, w, d) = ring.conj(x)
            return Zorn(r * a, Vec3(r * v.x, r * v.y, r * v.z), Vec3(r * w.x, r * w.y, r * w.z), r * d)
        }
        checkAll(ArbZorn.unitRationalZorn, arb) { x, y ->
            val xInv = inverse(x)
            ring.mul(x, xInv) shouldBe ring.one
            ring.mul(xInv, x) shouldBe ring.one
            ring.mul(xInv, ring.mul(x, y)) shouldBe y
            ring.mul(ring.mul(y, x), xInv) shouldBe y
        }
    }

    "zero divisors: the diagonal idempotents e = (1, 0, 0, 0) and f = (0, 0, 0, 1)" {
        val e = Zorn(one, zeroVec, zeroVec, zero)
        val f = Zorn(zero, zeroVec, zeroVec, one)
        ring.mul(e, e) shouldBe e
        ring.mul(f, f) shouldBe f
        ring.mul(e, f) shouldBe ring.zero
        ring.mul(f, e) shouldBe ring.zero
        ring.add(e, f) shouldBe ring.one
        normSq(e) shouldBe zero
    }
})
