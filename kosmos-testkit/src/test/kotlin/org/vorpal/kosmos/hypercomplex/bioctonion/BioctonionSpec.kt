package org.vorpal.kosmos.hypercomplex.bioctonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.ArbBiquaternion
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Tests of the bioctonions `𝕆 ⊗ ℂ`.
 *
 * The coefficients are floating-point complex numbers, so the laws are checked up to approximate
 * equality. The same construction is checked *exactly*, over `ℚ(i)`, in `ComplexifiedTowerSpec`.
 */
class BioctonionSpec : FunSpec({
    lawTests(lawGraph("bioctonions") {
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

        branch("BioctonionAlgebra satisfies NonAssociativeInvolutiveRingLaws") {
            suite(NonAssociativeInvolutiveRingLaws(ring, arb, eq, pr))
        }

        branch("bioctonion multiplication is alternative") {
            suite(AlternativityLaw(ring.mul.op, arb, eq, pr))
        }

        law("bioctonion multiplication is flexible", FlexibilityLaw(ring.mul.op, arb, eq, pr))

        branch("bioctonion multiplication satisfies the Moufang identities") {
            suite(MoufangLaw(ring.mul.op, arb, eq, pr))
        }

        local("bioctonion multiplication is not associative: (e₁e₂)e₄ = -e₁(e₂e₄)") {
            val (e1, e2, e4) = listOf(1, 2, 4).map(ring.basis::get)
            val left = ring.mul(ring.mul(e1, e2), e4)
            val right = ring.mul(e1, ring.mul(e2, e4))
            eq(left, right) shouldBe false
            lawCheck(eq(left, neg(right)))
        }

        local("the basis has 8 elements and e₁, …, e₇ all square to -1") {
            ring.basis.size shouldBe 8
            ring.basis[0] shouldBe one
            ring.basis.drop(1).forEach { e -> lawCheck(eq(ring.mul(e, e), ring.negOne)) }
            ring.basisMap.keys shouldBe (0..7).toSet()
        }

        local("h² = -1, and h is central and nuclear") {
            lawCheck(eq(ring.mul(h, h), ring.negOne))
            lawCheckAll(arb, arb) { x, y ->
                lawCheck(eq(ring.mul(h, x), ring.mul(x, h)))
                lawCheck(eq(ring.mul(ring.mul(h, x), y), ring.mul(h, ring.mul(x, y))))
                lawCheck(eq(ring.mul(ring.mul(x, h), y), ring.mul(x, ring.mul(h, y))))
                lawCheck(eq(ring.mul(ring.mul(x, y), h), ring.mul(x, ring.mul(y, h))))
            }
        }

        local("coefficients round-trip through the constructor") {
            lawCheckAll(arb) { o ->
                bioctonion(o.coefficients()) shouldBe o
                (0..7).map { n -> o[n] } shouldBe o.coefficients()
            }
            shouldThrow<IllegalArgumentException> { bioctonion(List(7) { complexField.zero }) }
        }

        local("the octonion conjugate negates e₁, …, e₇ and leaves the coefficients unconjugated") {
            lawCheckAll(arb) { o ->
                val cs = o.coefficients()
                lawCheck(eq(ring.conj(o), bioctonion(listOf(cs[0]) + cs.drop(1).map(complexField.add.inverse::invoke))))
            }
        }

        local("the complex conjugate is an involutive automorphism: it does not reverse products") {
            lawCheckAll(arb, arb) { p, q ->
                lawCheck(eq(ring.complexConj(ring.complexConj(p)), p))
                lawCheck(eq(ring.complexConj(ring.add(p, q)), ring.add(ring.complexConj(p), ring.complexConj(q))))
                lawCheck(eq(ring.complexConj(ring.mul(p, q)), ring.mul(ring.complexConj(p), ring.complexConj(q))))
            }
            lawCheck(eq(ring.complexConj(h), neg(h)))
            ring.basis.forEach { e -> lawCheck(eq(ring.complexConj(e), e)) }
        }

        local("normSq formula: N(o) = c₀² + … + c₇² ∈ ℂ, with no conjugation") {
            lawCheckAll(arb) { o ->
                val expected = o.coefficients().map { c -> complexField.mul(c, c) }.reduce(complexField.add::invoke)
                lawCheck(eqC(ring.normSq(o), expected))
            }
        }

        local("the norm is the scalar o·conj(o) = conj(o)·o") {
            val embed = BioctonionAlgebras.ComplexToBioctonionMonomorphism
            lawCheckAll(arb) { o ->
                lawCheck(eq(ring.mul(o, ring.conj(o)), embed(ring.normSq(o))))
                lawCheck(eq(ring.mul(ring.conj(o), o), embed(ring.normSq(o))))
            }
        }

        law("normSq is multiplicative: the bioctonions are a composition algebra over ℂ", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = complexField.mul.op,
                arbA = arb,
                eqN = eqC,
                prA = pr,
                prN = prC
            ))

        local("zero divisors: (1 + h·e_n)(1 - h·e_n) = 0 for every imaginary unit") {
            ring.basis.drop(1).forEach { e ->
                val he = ring.mul(h, e)
                val plus = ring.add(one, he)
                val minus = ring.add(one, neg(he))
                lawCheck(eq(ring.mul(plus, minus), ring.zero))
                ring.hasReciprocal(plus) shouldBe false
                shouldThrow<ArithmeticException> { ring.reciprocal(plus) }
            }
            shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
        }

        local("reciprocal: o·o⁻¹ = o⁻¹·o = 1 away from the null cone") {
            lawCheckAll(ArbBioctonion.unitBioctonion) { o ->
                val inverse = ring.reciprocal(o)
                lawCheck(eq(ring.mul(o, inverse), one))
                lawCheck(eq(ring.mul(inverse, o), one))
            }
        }

        branch("BioctonionStarAlgebra satisfies NonAssociativeStarAlgebraLaws over ℂ") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = BioctonionAlgebras.BioctonionStarAlgebra,
                scalarArb = ArbBiquaternion.smallComplex,
                algebraArb = arb,
                eqR = eqC,
                eqA = eq,
                prR = prC,
                prA = pr
            ))
        }

        branch("BioctonionHermitianStarAlgebra satisfies NonAssociativeStarAlgebraLaws over ℝ") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = BioctonionAlgebras.BioctonionHermitianStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = arb,
                eqR = RealAlgebras.eqRealApprox,
                eqA = eq,
                prR = RealAlgebras.printableRealPretty,
                prA = pr
            ))
        }

        branch("the bioctonions are a vector space of dimension 8 over ℂ and 16 over ℝ") {
            suite(VectorSpaceLaws(
                space = BioctonionAlgebras.BioctonionComplexVectorSpace,
                scalarArb = ArbBiquaternion.smallComplex,
                vectorArb = arb,
                eqF = eqC,
                eqV = eq,
                prF = prC,
                prV = pr
            ))
            suite(VectorSpaceLaws(
                space = BioctonionAlgebras.BioctonionRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = arb,
                eqF = RealAlgebras.eqRealApprox,
                eqV = eq,
                prF = RealAlgebras.printableRealPretty,
                prV = pr
            ))
            local("dimensions") {
                BioctonionAlgebras.BioctonionComplexVectorSpace.dimension shouldBe 8
                BioctonionAlgebras.BioctonionRealVectorSpace.dimension shouldBe 16
            }
        }

        local("printable shows 0 for zero and one parenthesized coefficient per nonzero term") {
            BioctonionAlgebras.printableBioctonion(ring.zero) shouldBe "0"
            BioctonionAlgebras.printableBioctonion(ring.add(one, ring.basis[5])).count { it == '(' } shouldBe 2
        }
    })
})
