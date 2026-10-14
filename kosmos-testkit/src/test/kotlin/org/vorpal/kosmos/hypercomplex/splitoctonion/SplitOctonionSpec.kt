package org.vorpal.kosmos.hypercomplex.splitoctonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Exact tests of the split-octonions over ℚ.
 *
 * Arithmetic is exact, so alternativity, the Moufang identities and multiplicativity of the split
 * norm are verified with strict equality.
 */
class SplitOctonionSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = SplitOctonionAlgebras.splitOctonionRing(q)
    val withReciprocal = SplitOctonionAlgebras.splitOctonionRingWithReciprocal(q)
    val normSq = SplitOctonionAlgebras.normSq(q)
    val arb = ArbSplitOctonion.rationalSplitOctonion
    val pr = SplitOctonionAlgebras.printableSplitOctonion(
        q, RationalAlgebras.SignedRational, RationalAlgebras.printableRational, RationalAlgebras.eqRational
    )

    val basis = SplitOctonionAlgebras.basis(q)
    val one = ring.one
    fun neg(x: SplitOctonion<Rational>) = ring.add.inverse(x)

    "the split-octonions over ℚ satisfy NonAssociativeInvolutiveRingLaws" {
        NonAssociativeInvolutiveRingLaws(ring, arb, pr = pr).fullTest().throwIfFailed()
    }

    "split-octonion multiplication is exactly alternative" {
        AlternativityLaw(ring.mul.op, arb, pr = pr).test()
    }

    "split-octonion multiplication is exactly flexible" {
        FlexibilityLaw(ring.mul.op, arb, pr = pr).test()
    }

    "split-octonion multiplication exactly satisfies the Moufang identities" {
        MoufangLaw(ring.mul.op, arb, pr = pr).test()
    }

    "split-octonion multiplication is not associative: (e₁e₂)e₄ = -e₁(e₂e₄)" {
        val (e1, e2, e4) = listOf(1, 2, 4).map(basis::get)
        val left = ring.mul(ring.mul(e1, e2), e4)
        val right = ring.mul(e1, ring.mul(e2, e4))
        (left == right) shouldBe false
        left shouldBe neg(right)
    }

    "the basis has 8 elements; e₁, e₂, e₃ square to -1 and e₄, …, e₇ square to +1" {
        basis.size shouldBe 8
        basis[0] shouldBe one
        basis.subList(1, 4).forEach { e -> ring.mul(e, e) shouldBe ring.negOne }
        basis.subList(4, 8).forEach { e -> ring.mul(e, e) shouldBe one }
    }

    "distinct imaginary basis units anticommute, and their product is ± a basis unit" {
        val signedUnits = basis.drop(1).flatMap { e -> listOf(e, neg(e)) }.toSet()
        (1..7).forEach { a ->
            (1..7).filter { b -> b != a }.forEach { b ->
                val ab = ring.mul(basis[a], basis[b])
                ab shouldBe neg(ring.mul(basis[b], basis[a]))
                (ab in signedUnits) shouldBe true
            }
        }
    }

    "conjugation negates the seven imaginary coordinates" {
        checkAll(arb) { x ->
            val cs = x.coefficients()
            ring.conj(x).coefficients() shouldBe listOf(cs[0]) + cs.drop(1).map { c -> -c }
        }
    }

    "normSq formula: N = w² + x² + y² + z² - u² - v² - s² - t² (signature (4, 4))" {
        checkAll(arb) { o ->
            val squares = o.coefficients().map { c -> c * c }
            normSq(o) shouldBe squares.take(4).reduce(Rational::plus) - squares.drop(4).reduce(Rational::plus)
        }
    }

    "the norm is the scalar o·conj(o) = conj(o)·o" {
        val embed = SplitOctonionAlgebras.scalarEmbedding(q)
        checkAll(arb) { o ->
            ring.mul(o, ring.conj(o)) shouldBe embed(normSq(o))
            ring.mul(ring.conj(o), o) shouldBe embed(normSq(o))
        }
    }

    "normSq is exactly multiplicative: the split-octonions are a composition algebra" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = normSq,
            mulN = q.mul.op,
            arbA = arb,
            prA = pr
        ).test()
    }

    "zero divisors: (1 + e_n)(1 - e_n) = 0 for n = 4, …, 7" {
        basis.subList(4, 8).forEach { e ->
            val plus = ring.add(one, e)
            val minus = ring.add(one, neg(e))
            ring.mul(plus, minus) shouldBe ring.zero
            normSq(plus) shouldBe Rational.ZERO
        }
    }

    "reciprocal: o⁻¹ = conj(o)/N(o) is a two-sided inverse off the null cone" {
        val action = SplitOctonionAlgebras.splitOctonionLeftAction(q)
        checkAll(ArbSplitOctonion.unitRationalSplitOctonion) { o ->
            withReciprocal.hasReciprocal(o) shouldBe true
            val inverse = withReciprocal.reciprocal(o)
            inverse shouldBe action(normSq(o).reciprocal(), ring.conj(o))
            ring.mul(o, inverse) shouldBe one
            ring.mul(inverse, o) shouldBe one
        }
    }

    "the inverse property holds despite nonassociativity: x⁻¹(xy) = y = (yx)x⁻¹" {
        checkAll(ArbSplitOctonion.unitRationalSplitOctonion, arb) { x, y ->
            val inverse = withReciprocal.reciprocal(x)
            ring.mul(inverse, ring.mul(x, y)) shouldBe y
            ring.mul(ring.mul(y, x), inverse) shouldBe y
        }
    }

    "the null cone is not invertible" {
        val nullElement = ring.add(one, basis[4])
        withReciprocal.hasReciprocal(nullElement) shouldBe false
        withReciprocal.hasReciprocal(ring.zero) shouldBe false
        shouldThrow<ArithmeticException> { withReciprocal.reciprocal(nullElement) }
        shouldThrow<ArithmeticException> { withReciprocal.reciprocal(ring.zero) }
    }

    "the split-octonions over ℚ are a free ℚ-module satisfying RModuleLaws" {
        RModuleLaws(
            module = SplitOctonionAlgebras.splitOctonionModule(q),
            scalarArb = ArbSplitQuaternion.smallRational,
            vectorArb = arb,
            prM = pr
        ).fullTest().throwIfFailed()
    }

    "the split-octonions over ℚ satisfy NonAssociativeStarAlgebraLaws" {
        NonAssociativeStarAlgebraLaws(
            algebra = SplitOctonionAlgebras.splitOctonionStarAlgebra(q),
            scalarArb = ArbSplitQuaternion.smallRational,
            algebraArb = arb,
            prA = pr
        ).fullTest().throwIfFailed()
    }

    "the scalar embedding ℚ ↪ SplitOctonion<ℚ> is an injective unital homomorphism" {
        val embed = SplitOctonionAlgebras.scalarEmbedding(q)
        NonAssociativeRingHomomorphismLaws(
            hom = embed::invoke,
            domain = q,
            codomain = ring,
            arb = ArbSplitQuaternion.smallRational
        ).fullTest().throwIfFailed()

        injectivityLaw(hom = embed::invoke, arbA = ArbSplitQuaternion.smallRational).test()
    }

    "ℍ' ↪ 𝕆' is an injective unital homomorphism sending i, j, k to e₁, e₄, e₅" {
        val mono = SplitOctonionAlgebras.splitQuaternionToSplitOctonionMonomorphism(q)
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = SplitQuaternionAlgebras.splitQuaternionRing(q),
            codomain = ring,
            arb = ArbSplitQuaternion.rationalSplitQuaternion
        ).fullTest().throwIfFailed()

        injectivityLaw(hom = mono::invoke, arbA = ArbSplitQuaternion.rationalSplitQuaternion).test()

        val (_, i, j, k) = SplitQuaternionAlgebras.basis(q)
        mono(i) shouldBe basis[1]
        mono(j) shouldBe basis[4]
        mono(k) shouldBe basis[5]
    }

    "ℍ' ↪ 𝕆' preserves the norm" {
        val mono = SplitOctonionAlgebras.splitQuaternionToSplitOctonionMonomorphism(q)
        val quaternionNormSq = SplitQuaternionAlgebras.normSq(q)
        checkAll(ArbSplitQuaternion.rationalSplitQuaternion) { x ->
            normSq(mono(x)) shouldBe quaternionNormSq(x)
        }
    }
})
