package org.vorpal.kosmos.hypercomplex.biquaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Tests of the biquaternions `ℍ ⊗ ℂ`.
 *
 * The coefficients are floating-point complex numbers, so the laws are checked up to approximate
 * equality. The same construction is checked *exactly*, over `ℚ(i)`, in `ComplexifiedTowerSpec`.
 */
class BiquaternionSpec : StringSpec({
    val ring = BiquaternionAlgebras.BiquaternionRing
    val eq = BiquaternionAlgebras.eqBiquaternion
    val pr = BiquaternionAlgebras.printableBiquaternionPretty
    val arb = ArbBiquaternion.biquaternion
    val complexField = ComplexAlgebras.ComplexField
    val eqC = ComplexAlgebras.eqComplex
    val prC = ComplexAlgebras.printableComplexPretty

    val one = ring.one
    val (i, j, k) = Triple(ring.i, ring.j, ring.k)
    val h = ring.h
    fun neg(x: Biquaternion) = ring.add.inverse(x)

    "BiquaternionRing satisfies InvolutiveRingLaws (associative, with the quaternion conjugate)" {
        InvolutiveRingLaws(ring, arb, eq, pr).fullTest().throwIfFailed()
    }

    "i² = j² = k² = ijk = -1 and ij = k = -ji" {
        listOf(i, j, k).forEach { u -> check(eq(ring.mul(u, u), ring.negOne)) }
        check(eq(ring.mul(ring.mul(i, j), k), ring.negOne))
        check(eq(ring.mul(i, j), k))
        check(eq(ring.mul(j, i), neg(k)))
        ring.basis shouldBe listOf(one, i, j, k)
    }

    "h² = -1 and h is central" {
        check(eq(ring.mul(h, h), ring.negOne))
        checkAll(arb) { q ->
            check(eq(ring.mul(h, q), ring.mul(q, h)))
        }
    }

    "the quaternion conjugate negates the i, j, k coefficients and leaves them unconjugated" {
        checkAll(arb) { q ->
            val neg = complexField.add.inverse
            check(eq(ring.conj(q), biquaternion(q.w, neg(q.x), neg(q.y), neg(q.z))))
        }
    }

    "the complex conjugate is an involutive ring automorphism: it does not reverse products" {
        checkAll(arb, arb) { p, q ->
            check(eq(ring.complexConj(ring.complexConj(p)), p))
            check(eq(ring.complexConj(ring.add(p, q)), ring.add(ring.complexConj(p), ring.complexConj(q))))
            check(eq(ring.complexConj(ring.mul(p, q)), ring.mul(ring.complexConj(p), ring.complexConj(q))))
        }
        check(eq(ring.complexConj(h), neg(h)))
        listOf(one, i, j, k).forEach { u -> check(eq(ring.complexConj(u), u)) }
    }

    "the Hermitian conjugate is the composite of the other two, in either order" {
        checkAll(arb) { q ->
            check(eq(ring.hermitianConj(q), ring.complexConj(ring.conj(q))))
            check(eq(ring.hermitianConj(q), ring.conj(ring.complexConj(q))))
        }
    }

    "normSq formula: N(q) = w² + x² + y² + z² ∈ ℂ, with no conjugation" {
        checkAll(arb) { q ->
            val expected = q.coefficients().map { c -> complexField.mul(c, c) }.reduce(complexField.add::invoke)
            check(eqC(ring.normSq(q), expected))
        }
    }

    "the norm is the scalar q·conj(q) = conj(q)·q" {
        val embed = BiquaternionAlgebras.ComplexToBiquaternionMonomorphism
        checkAll(arb) { q ->
            check(eq(ring.mul(q, ring.conj(q)), embed(ring.normSq(q))))
            check(eq(ring.mul(ring.conj(q), q), embed(ring.normSq(q))))
        }
    }

    "normSq is multiplicative: N(pq) = N(p)N(q)" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = ring.normSq,
            mulN = complexField.mul.op,
            arbA = arb,
            eqN = eqC,
            prA = pr,
            prN = prC
        ).test()
    }

    "zero divisors: (1 + h·i)(1 - h·i) = 0, and both factors have norm 0" {
        val hi = ring.mul(h, i)
        val plus = ring.add(one, hi)
        val minus = ring.add(one, neg(hi))
        check(eq(ring.mul(plus, minus), ring.zero))
        check(eqC(ring.normSq(plus), complexField.zero))
        ring.hasReciprocal(plus) shouldBe false
        shouldThrow<ArithmeticException> { ring.reciprocal(plus) }
        shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
    }

    "reciprocal: q·q⁻¹ = q⁻¹·q = 1 away from the null cone" {
        checkAll(ArbBiquaternion.unitBiquaternion) { q ->
            val inverse = ring.reciprocal(q)
            check(eq(ring.mul(q, inverse), one))
            check(eq(ring.mul(inverse, q), one))
        }
    }

    "BiquaternionStarAlgebra satisfies StarAlgebraLaws over ℂ" {
        StarAlgebraLaws(
            algebra = BiquaternionAlgebras.BiquaternionStarAlgebra,
            scalarArb = ArbBiquaternion.smallComplex,
            algebraArb = arb,
            eqR = eqC,
            eqA = eq,
            prR = prC,
            prA = pr
        ).test().throwIfFailed()
    }

    "BiquaternionHermitianStarAlgebra satisfies StarAlgebraLaws over ℝ" {
        StarAlgebraLaws(
            algebra = BiquaternionAlgebras.BiquaternionHermitianStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = arb,
            eqR = RealAlgebras.eqRealApprox,
            eqA = eq,
            prR = RealAlgebras.printableRealPretty,
            prA = pr
        ).test().throwIfFailed()
    }

    "the Hermitian conjugate is ℂ-antilinear: (c ⊳ q)† = conj(c) ⊳ q†" {
        val action = BiquaternionAlgebras.BiquaternionComplexVectorSpace.leftAction
        val conjC = ComplexAlgebras.ComplexStarAlgebra.conj
        checkAll(ArbBiquaternion.smallComplex, arb) { c, q ->
            check(eq(ring.hermitianConj(action(c, q)), action(conjC(c), ring.hermitianConj(q))))
        }
    }

    "the biquaternions are a vector space of dimension 4 over ℂ and 8 over ℝ" {
        BiquaternionAlgebras.BiquaternionComplexVectorSpace.dimension shouldBe 4
        BiquaternionAlgebras.BiquaternionRealVectorSpace.dimension shouldBe 8

        VectorSpaceLaws(
            space = BiquaternionAlgebras.BiquaternionComplexVectorSpace,
            scalarArb = ArbBiquaternion.smallComplex,
            vectorArb = arb,
            eqF = eqC,
            eqV = eq,
            prF = prC,
            prV = pr
        ).test().throwIfFailed()

        VectorSpaceLaws(
            space = BiquaternionAlgebras.BiquaternionRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = arb,
            eqF = RealAlgebras.eqRealApprox,
            eqV = eq,
            prF = RealAlgebras.printableRealPretty,
            prV = pr
        ).test().throwIfFailed()
    }

    "printable shows complex coefficients against the 1, I, J, K basis, and 0 for zero" {
        BiquaternionAlgebras.printableBiquaternion(ring.zero) shouldBe "0"
        val rendered = BiquaternionAlgebras.printableBiquaternion(
            biquaternion(complex(1.0, 2.0), complexField.zero, complex(0.0, -1.0), complexField.zero)
        )
        rendered.count { it == '(' } shouldBe 2
    }
})
