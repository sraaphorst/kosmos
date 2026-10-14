package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.products.CommutativeRings
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import java.math.BigInteger

/**
 * The idempotent splitting `Φ : Bicomplex(R) → Complex(R) × Complex(R)`, `z ↦ (𝛂, 𝜷)`.
 *
 * `Φ` is a ring homomorphism over every commutative base ring, but an isomorphism only when 2 is
 * invertible:
 * - over ℚ it is an isomorphism, with inverse `ofIdempotent`, and gives a second, independent way to
 *   multiply bicomplex numbers (componentwise);
 * - over ℤ it is injective but not surjective;
 * - over `𝔽₂` the two projections coincide and it is not injective.
 */
class BicomplexIdempotentSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val bicomplex = BicomplexAlgebras.bicomplexRing(q)
    val complexQ = ComplexAlgebras.complexRing(q)
    val product = CommutativeRings.double(complexQ)

    val smallRational: Arb<Rational> =
        Arb.bind(Arb.int(-8..8), Arb.int(1..5)) { p, d -> Rational.of(p, d) }
    val arbComplex: Arb<Complex<Rational>> =
        Arb.bind(smallRational, smallRational) { a, b -> complex(a, b) }
    val arbBicomplex: Arb<Bicomplex<Rational>> =
        Arb.bind(arbComplex, arbComplex) { z1, z2 -> Bicomplex.ofStandard(z1, z2) }
    val arbPair: Arb<Pair<Complex<Rational>, Complex<Rational>>> =
        Arb.bind(arbComplex, arbComplex) { a, b -> a to b }

    val phi = BicomplexAlgebras.idempotentHomomorphism(q)
    val iso = BicomplexAlgebras.idempotentIsomorphism(q)

    "destructuring a bicomplex number gives its two standard complex components" {
        checkAll(arbBicomplex) { z ->
            val (z1, z2) = z
            z1 shouldBe z.z1
            z2 shouldBe z.z2
            z.standard() shouldBe (z1 to z2)
        }
    }

    "Φ over ℚ is a unital ring homomorphism into the product ring Complex(ℚ) × Complex(ℚ)" {
        RingHomomorphismLaws(
            hom = phi::invoke,
            domain = bicomplex,
            codomain = product,
            arb = arbBicomplex
        ).fullTest().throwIfFailed()
    }

    "Φ(zw) = Φ(z)Φ(w) componentwise: an independent way to multiply bicomplex numbers" {
        checkAll(arbBicomplex, arbBicomplex) { z, w ->
            val (a1, b1) = phi(z)
            val (a2, b2) = phi(w)
            phi(bicomplex.mul(z, w)) shouldBe (complexQ.mul(a1, a2) to complexQ.mul(b1, b2))
            // ... and multiplying in idempotent coordinates, then coming back, is the same product.
            bicomplex.mul(z, w) shouldBe
                BicomplexAlgebras.ofIdempotent(q, complexQ.mul(a1, a2), complexQ.mul(b1, b2))
        }
    }

    "over ℚ, Φ and ofIdempotent are mutually inverse" {
        checkAll(arbBicomplex) { z ->
            val (alpha, beta) = phi(z)
            BicomplexAlgebras.ofIdempotent(q, alpha, beta) shouldBe z
            iso.backward(iso(z)) shouldBe z
        }
        checkAll(arbPair) { pair ->
            phi(BicomplexAlgebras.ofIdempotent(q, pair.first, pair.second)) shouldBe pair
            iso(iso.backward(pair)) shouldBe pair
        }
    }

    "over ℚ, the inverse Complex(ℚ) × Complex(ℚ) → Bicomplex(ℚ) is a unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = iso.backward::invoke,
            domain = product,
            codomain = bicomplex,
            arb = arbPair
        ).fullTest().throwIfFailed()
    }

    "the orthogonal idempotents are e₁ = (1 - ij)/2 ↦ (1, 0) and e₂ = (1 + ij)/2 ↦ (0, 1)" {
        val half = Rational.of(1, 2)
        val zero = Rational.ZERO
        val e1 = Bicomplex.ofStandard(half, zero, zero, -half)
        val e2 = Bicomplex.ofStandard(half, zero, zero, half)

        phi(e1) shouldBe (complexQ.one to complexQ.zero)
        phi(e2) shouldBe (complexQ.zero to complexQ.one)
        bicomplex.mul(e1, e1) shouldBe e1
        bicomplex.mul(e2, e2) shouldBe e2
        bicomplex.mul(e1, e2) shouldBe bicomplex.zero
        bicomplex.add(e1, e2) shouldBe bicomplex.one

        // z = 𝛂·e₁ + 𝜷·e₂
        val embed = BicomplexAlgebras.complexToBicomplexMonomorphism(q)
        checkAll(arbBicomplex) { z ->
            val (alpha, beta) = phi(z)
            bicomplex.add(bicomplex.mul(embed(alpha), e1), bicomplex.mul(embed(beta), e2)) shouldBe z
        }
    }

    "the tessarine unit j_tess = i·j squares to +1, and j = -i·j_tess, over ℚ and over ℤ" {
        fun <R : Any> checkTessarineBasis(base: CommutativeRing<R>) {
            val ring = BicomplexAlgebras.bicomplexRing(base)
            val (one, i, j, k) = BicomplexAlgebras.basis(base)
            val jTess = ring.mul(i, j)
            jTess shouldBe k
            ring.mul(jTess, jTess) shouldBe one
            ring.add.inverse(ring.mul(i, jTess)) shouldBe j
        }
        checkTessarineBasis(q)
        checkTessarineBasis(IntegerAlgebras.IntegerCommutativeRing)
    }

    "over ℤ, Φ is injective but not surjective: 𝛂 - 𝜷 = 2i·z2 is always even" {
        val z = IntegerAlgebras.IntegerCommutativeRing
        val phiZ = BicomplexAlgebras.idempotentHomomorphism(z)
        val arbInt = Arb.int(-20..20).map(Int::toBigInteger)
        val arbZ = Arb.bind(arbInt, arbInt, arbInt, arbInt) { a, b, c, d -> Bicomplex.ofStandard(a, b, c, d) }
        val two = BigInteger.TWO

        injectivityLaw(hom = phiZ::invoke, arbA = arbZ).test()
        checkAll(arbZ) { x ->
            val (alpha, beta) = phiZ(x)
            (alpha.re - beta.re).mod(two) shouldBe BigInteger.ZERO
            (alpha.im - beta.im).mod(two) shouldBe BigInteger.ZERO
        }
        // So (1, 0) is not in the image. And 2 ↦ (2, 2): both coordinates nonzero, yet 2 is not a unit.
        phiZ(Bicomplex.ofStandard(two, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)) shouldBe
            (complex(two, BigInteger.ZERO) to complex(two, BigInteger.ZERO))
    }

    "over 𝔽₂ the two projections coincide, so Φ is not injective, and the splitting is refused" {
        val f2 = PrimeField(BigInteger.TWO)
        val alpha = BicomplexAlgebras.firstProjection(f2)
        val beta = BicomplexAlgebras.secondProjection(f2)
        val bits = listOf(BigInteger.ZERO, BigInteger.ONE)
        val all = bits.flatMap { a -> bits.flatMap { b -> bits.flatMap { c -> bits.map { d ->
            Bicomplex.ofStandard(a, b, c, d)
        } } } }
        all.size shouldBe 16
        all.forEach { z -> alpha(z) shouldBe beta(z) }

        val phi2 = BicomplexAlgebras.idempotentHomomorphism(f2)
        (all.map { z -> phi2(z) }.toSet().size < 16) shouldBe true

        shouldThrow<IllegalArgumentException> { BicomplexAlgebras.idempotentIsomorphism(f2) }
        shouldThrow<IllegalArgumentException> {
            BicomplexAlgebras.ofIdempotent(f2, complex(BigInteger.ONE, BigInteger.ZERO), complex(BigInteger.ZERO, BigInteger.ZERO))
        }
    }

    "over ℝ, the generic ofIdempotent inverts the alpha and beta views" {
        val real = RealAlgebras.RealField
        checkAll(ArbBicomplex.bicomplex) { z ->
            check(BicomplexAlgebras.eqBicomplex(BicomplexAlgebras.ofIdempotent(real, z.alpha, z.beta), z))
            // Up to signed zeros: the generic route multiplies by i, the views use the closed form.
            val (alpha, beta) = BicomplexAlgebras.idempotentHomomorphism(real)(z)
            check(ComplexAlgebras.eqComplex(alpha, z.alpha))
            check(ComplexAlgebras.eqComplex(beta, z.beta))
        }
        BicomplexAlgebras.BicomplexCommutativeRing.zero shouldBe Bicomplex.ofStandard(0.0, 0.0, 0.0, 0.0)
        BicomplexAlgebras.BicomplexCommutativeRing.one shouldBe Bicomplex.ofStandard(1.0, 0.0, 0.0, 0.0)
    }
})
