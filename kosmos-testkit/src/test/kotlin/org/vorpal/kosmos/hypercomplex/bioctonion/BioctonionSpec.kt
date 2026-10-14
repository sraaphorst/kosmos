package org.vorpal.kosmos.hypercomplex.bioctonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.ArbBiquaternion
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Tests of the bioctonions `𝕆 ⊗ ℂ`.
 *
 * The coefficients are floating-point complex numbers, so the laws are checked up to approximate
 * equality. The same construction is checked *exactly*, over `ℚ(i)`, in `ComplexifiedTowerSpec`.
 */
class BioctonionSpec : StringSpec({
    val ring = BioctonionAlgebras.BioctonionAlgebra
    val eq = BioctonionAlgebras.eqBioctonion
    val pr = BioctonionAlgebras.printableBioctonionPretty
    val arb = ArbBioctonion.bioctonion
    val complexField = ComplexAlgebras.ComplexField
    val eqC = ComplexAlgebras.eqComplex
    val prC = ComplexAlgebras.printableComplexPretty

    val one = ring.one
    val h = ring.h
    fun neg(x: RealBioctonion) = ring.add.inverse(x)

    "BioctonionAlgebra satisfies NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(ring, arb, eq, pr).fullTest().throwIfFailed()
    }

    "bioctonion multiplication is alternative" {
        AlternativityLaw(ring.mul.op, arb, eq, pr).test()
    }

    "bioctonion multiplication is flexible" {
        FlexibilityLaw(ring.mul.op, arb, eq, pr).test()
    }

    "bioctonion multiplication satisfies the Moufang identities" {
        MoufangLaw(ring.mul.op, arb, eq, pr).test()
    }

    "bioctonion multiplication is not associative: (e₁e₂)e₄ = -e₁(e₂e₄)" {
        val (e1, e2, e4) = listOf(1, 2, 4).map(ring.basis::get)
        val left = ring.mul(ring.mul(e1, e2), e4)
        val right = ring.mul(e1, ring.mul(e2, e4))
        eq(left, right) shouldBe false
        check(eq(left, neg(right)))
    }

    "the basis has 8 elements and e₁, …, e₇ all square to -1" {
        ring.basis.size shouldBe 8
        ring.basis[0] shouldBe one
        ring.basis.drop(1).forEach { e -> check(eq(ring.mul(e, e), ring.negOne)) }
        ring.basisMap.keys shouldBe (0..7).toSet()
    }

    "h² = -1, and h is central and nuclear" {
        check(eq(ring.mul(h, h), ring.negOne))
        checkAll(arb, arb) { x, y ->
            check(eq(ring.mul(h, x), ring.mul(x, h)))
            check(eq(ring.mul(ring.mul(h, x), y), ring.mul(h, ring.mul(x, y))))
            check(eq(ring.mul(ring.mul(x, h), y), ring.mul(x, ring.mul(h, y))))
            check(eq(ring.mul(ring.mul(x, y), h), ring.mul(x, ring.mul(y, h))))
        }
    }

    "coefficients round-trip through the constructor" {
        checkAll(arb) { o ->
            bioctonion(o.coefficients()) shouldBe o
            (0..7).map { n -> o[n] } shouldBe o.coefficients()
        }
        shouldThrow<IllegalArgumentException> { bioctonion(List(7) { complexField.zero }) }
    }

    "the octonion conjugate negates e₁, …, e₇ and leaves the coefficients unconjugated" {
        checkAll(arb) { o ->
            val cs = o.coefficients()
            check(eq(ring.conj(o), bioctonion(listOf(cs[0]) + cs.drop(1).map(complexField.add.inverse::invoke))))
        }
    }

    "the complex conjugate is an involutive automorphism: it does not reverse products" {
        checkAll(arb, arb) { p, q ->
            check(eq(ring.complexConj(ring.complexConj(p)), p))
            check(eq(ring.complexConj(ring.add(p, q)), ring.add(ring.complexConj(p), ring.complexConj(q))))
            check(eq(ring.complexConj(ring.mul(p, q)), ring.mul(ring.complexConj(p), ring.complexConj(q))))
        }
        check(eq(ring.complexConj(h), neg(h)))
        ring.basis.forEach { e -> check(eq(ring.complexConj(e), e)) }
    }

    "normSq formula: N(o) = c₀² + … + c₇² ∈ ℂ, with no conjugation" {
        checkAll(arb) { o ->
            val expected = o.coefficients().map { c -> complexField.mul(c, c) }.reduce(complexField.add::invoke)
            check(eqC(ring.normSq(o), expected))
        }
    }

    "the norm is the scalar o·conj(o) = conj(o)·o" {
        val embed = BioctonionAlgebras.ComplexToBioctonionMonomorphism
        checkAll(arb) { o ->
            check(eq(ring.mul(o, ring.conj(o)), embed(ring.normSq(o))))
            check(eq(ring.mul(ring.conj(o), o), embed(ring.normSq(o))))
        }
    }

    "normSq is multiplicative: the bioctonions are a composition algebra over ℂ" {
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

    "zero divisors: (1 + h·e_n)(1 - h·e_n) = 0 for every imaginary unit" {
        ring.basis.drop(1).forEach { e ->
            val he = ring.mul(h, e)
            val plus = ring.add(one, he)
            val minus = ring.add(one, neg(he))
            check(eq(ring.mul(plus, minus), ring.zero))
            ring.hasReciprocal(plus) shouldBe false
            shouldThrow<ArithmeticException> { ring.reciprocal(plus) }
        }
        shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
    }

    "reciprocal: o·o⁻¹ = o⁻¹·o = 1 away from the null cone" {
        checkAll(ArbBioctonion.unitBioctonion) { o ->
            val inverse = ring.reciprocal(o)
            check(eq(ring.mul(o, inverse), one))
            check(eq(ring.mul(inverse, o), one))
        }
    }

    "BioctonionStarAlgebra satisfies NonAssociativeStarAlgebraLaws over ℂ" {
        NonAssociativeStarAlgebraLaws(
            algebra = BioctonionAlgebras.BioctonionStarAlgebra,
            scalarArb = ArbBiquaternion.smallComplex,
            algebraArb = arb,
            eqR = eqC,
            eqA = eq,
            prR = prC,
            prA = pr
        ).test().throwIfFailed()
    }

    "BioctonionHermitianStarAlgebra satisfies NonAssociativeStarAlgebraLaws over ℝ" {
        NonAssociativeStarAlgebraLaws(
            algebra = BioctonionAlgebras.BioctonionHermitianStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = arb,
            eqR = RealAlgebras.eqRealApprox,
            eqA = eq,
            prR = RealAlgebras.printableRealPretty,
            prA = pr
        ).test().throwIfFailed()
    }

    "the bioctonions are a vector space of dimension 8 over ℂ and 16 over ℝ" {
        BioctonionAlgebras.BioctonionComplexVectorSpace.dimension shouldBe 8
        BioctonionAlgebras.BioctonionRealVectorSpace.dimension shouldBe 16

        VectorSpaceLaws(
            space = BioctonionAlgebras.BioctonionComplexVectorSpace,
            scalarArb = ArbBiquaternion.smallComplex,
            vectorArb = arb,
            eqF = eqC,
            eqV = eq,
            prF = prC,
            prV = pr
        ).test().throwIfFailed()

        VectorSpaceLaws(
            space = BioctonionAlgebras.BioctonionRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = arb,
            eqF = RealAlgebras.eqRealApprox,
            eqV = eq,
            prF = RealAlgebras.printableRealPretty,
            prV = pr
        ).test().throwIfFailed()
    }

    "printable shows 0 for zero and one parenthesized coefficient per nonzero term" {
        BioctonionAlgebras.printableBioctonion(ring.zero) shouldBe "0"
        BioctonionAlgebras.printableBioctonion(ring.add(one, ring.basis[5])).count { it == '(' } shouldBe 2
    }
})
