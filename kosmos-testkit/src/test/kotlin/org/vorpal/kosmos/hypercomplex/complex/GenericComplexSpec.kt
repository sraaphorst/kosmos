package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.dual.Dual
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import java.math.BigInteger

/**
 * The generic `Complex<R> = R[i]/(i² + 1)` construction, over base rings other than ℝ, and its
 * agreement with the named structures.
 *
 * - Over `𝔽₇`, where `-1` is not a square, `Complex<𝔽₇>` is the field with 49 elements.
 * - Over `𝔽₅`, where `2² = -1`, it has zero divisors.
 * - Over ℚ it is the same ring as `GaussianRatField`, and over ℝ the same as `ComplexField`.
 * - `Complex<Real>`, `SplitComplex<Real>` and `Dual<Real>` share a raw carrier but are distinct types.
 */
class GenericComplexSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val f5 = PrimeField(BigInteger.valueOf(5))
    val f7 = PrimeField(BigInteger.valueOf(7))

    fun arbOver(p: Int): Arb<Complex<BigInteger>> =
        Arb.bind(Arb.int(0 until p), Arb.int(0 until p)) { a, b -> complex(a.toBigInteger(), b.toBigInteger()) }

    val smallRational: Arb<Rational> =
        Arb.bind(Arb.int(-12..12), Arb.int(1..12)) { p, d -> Rational.of(p, d) }

    fun f(n: Int): BigInteger = n.toBigInteger()

    "Complex<𝔽₇> and Complex<𝔽₅> are commutative involutive rings" {
        listOf(f7 to 7, f5 to 5).forEach { (field, p) ->
            val ring = ComplexAlgebras.complexRing(field)
            CommutativeRingLaws(ring, arbOver(p)).fullTest().throwIfFailed()
            InvolutiveRingLaws(ring, arbOver(p)).fullTest().throwIfFailed()
        }
    }

    "i² = -1 over any base, and conjugation negates the imaginary part" {
        val ring = ComplexAlgebras.complexRing(f7)
        val i = ComplexAlgebras.imaginaryUnit(f7)
        i shouldBe complex(f(0), f(1))
        ring.mul(i, i) shouldBe ring.negOne
        checkAll(arbOver(7)) { z ->
            ring.conj(z) shouldBe complex(z.re, f7.add.inverse(z.im))
        }
    }

    "the norm a² + b² is z·conj(z), and is multiplicative" {
        val ring = ComplexAlgebras.complexRing(f7)
        val normSq = ComplexAlgebras.normSq(f7)
        checkAll(arbOver(7)) { z ->
            ring.mul(z, ring.conj(z)) shouldBe complex(normSq(z), f(0))
        }
        NormSqMultiplicativeLaw(ring.mul.op, normSq, f7.mul.op, arbOver(7)).test()
    }

    "Complex<𝔽₇> is a field with 49 elements: every nonzero element has norm ≠ 0 and an inverse" {
        val ring = ComplexAlgebras.complexRing(f7)
        val normSq = ComplexAlgebras.normSq(f7)
        val elements = (0..6).flatMap { a -> (0..6).map { b -> complex(f(a), f(b)) } }
        elements.toSet().size shouldBe 49

        elements.filter { z -> z != ring.zero }.forEach { z ->
            (normSq(z) == BigInteger.ZERO) shouldBe false
            val inverse = ComplexAlgebras.complexLeftAction(f7)(f7.reciprocal(normSq(z)), ring.conj(z))
            ring.mul(z, inverse) shouldBe ring.one
        }
    }

    "Complex<𝔽₅> has zero divisors, because 2² = -1 in 𝔽₅: (1 + 2i)(1 - 2i) = 0" {
        val ring = ComplexAlgebras.complexRing(f5)
        val z = complex(f(1), f(2))
        ring.mul(z, ring.conj(z)) shouldBe ring.zero
        ComplexAlgebras.normSq(f5)(z) shouldBe BigInteger.ZERO
        (z == ring.zero) shouldBe false
    }

    "Complex<ℚ> is a free module and a star algebra over ℚ" {
        RModuleLaws(ComplexAlgebras.complexModule(q), smallRational, ArbGaussianRat.small).fullTest().throwIfFailed()
        StarAlgebraLaws(ComplexAlgebras.complexStarAlgebra(q), smallRational, ArbGaussianRat.small)
            .fullTest().throwIfFailed()
    }

    "the scalar embedding ℚ ↪ Complex<ℚ> is an injective unital ring homomorphism" {
        val embed = ComplexAlgebras.scalarEmbedding(q)
        RingHomomorphismLaws(
            hom = embed::invoke,
            domain = q,
            codomain = ComplexAlgebras.complexRing(q),
            arb = smallRational
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = embed::invoke, arbA = smallRational).test()
    }

    "the generic complex ring over ℚ is GaussianRatField" {
        val generic = ComplexAlgebras.complexRing(q)
        val named = GaussianRatAlgebras.GaussianRatField
        generic.zero shouldBe named.zero
        generic.one shouldBe named.one
        checkAll(ArbGaussianRat.small, ArbGaussianRat.small) { a, b ->
            generic.add(a, b) shouldBe named.add(a, b)
            generic.mul(a, b) shouldBe named.mul(a, b)
            generic.conj(a) shouldBe GaussianRatAlgebras.GaussianRatStarAlgebra.conj(a)
            ComplexAlgebras.normSq(q)(a) shouldBe GaussianRatAlgebras.GaussianRatStarAlgebra.normSq(a)
        }
    }

    "the generic complex ring over ℝ is ComplexField" {
        val generic = ComplexAlgebras.complexRing(RealAlgebras.RealField)
        val named = ComplexAlgebras.ComplexField
        checkAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { a, b ->
            generic.add(a, b) shouldBe named.add(a, b)
            generic.mul(a, b) shouldBe named.mul(a, b)
            generic.conj(a) shouldBe ComplexAlgebras.ComplexStarAlgebra.conj(a)
        }
    }

    "generic Eq and Printable instances work over any base" {
        val eq = ComplexAlgebras.eqComplex(RationalAlgebras.eqRational)
        val z = complex(Rational.of(1, 2), Rational.of(-3))
        eq(z, complex(Rational.of(2, 4), Rational.of(-3))) shouldBe true
        eq(z, ComplexAlgebras.complexRing(q).conj(z)) shouldBe false

        val pr = ComplexAlgebras.printableComplex(
            q, RationalAlgebras.SignedRational, RationalAlgebras.printableRational, RationalAlgebras.eqRational
        )
        pr(z) shouldBe GaussianRatAlgebras.printableGaussianRat(z)
    }

    "Complex wraps CD: accessors, components and round-trip" {
        val z = complex(3, -4)
        z.cd shouldBe CD(3, -4)
        Complex(z.cd) shouldBe z
        val (re, im) = z
        (re to im) shouldBe (3 to -4)
        (z.re to z.im) shouldBe (3 to -4)
        z.toString() shouldBe "Complex(re=3, im=-4)"
    }

    "Complex<Real>, SplitComplex<Real> and Dual<Real> share a raw carrier but are distinct algebras" {
        val raw = CD(0.0, 1.0)
        val complexUnit = Complex(raw)
        val splitUnit = SplitComplex(raw)
        val dualUnit = Dual(raw)

        val any: List<Any> = listOf(complexUnit, splitUnit, dualUnit)
        any[0] shouldNotBe any[1]
        any[0] shouldNotBe any[2]
        any[1] shouldNotBe any[2]

        val complexField = ComplexAlgebras.ComplexField
        val split = SplitComplexAlgebras.RealSplitComplexRing
        val duals = DualAlgebras.DualRealRing

        // The new unit squares to -1, +1 and 0 respectively.
        check(ComplexAlgebras.eqComplex(complexField.mul(complexUnit, complexUnit), complexField.negOne))
        check(SplitComplexAlgebras.eqSplitComplex(split.mul(splitUnit, splitUnit), split.one))
        duals.mul(dualUnit, dualUnit).cd shouldBe CD(0.0, 0.0)
    }
})
