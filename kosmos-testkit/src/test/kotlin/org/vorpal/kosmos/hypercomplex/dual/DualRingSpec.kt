package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Exact tests of the dual numbers `R[ε]/(ε²)` over ℚ, ℤ and the finite field `𝔽₅`.
 *
 * Arithmetic is exact, so every law is verified with strict equality. The floating-point structures
 * over ℝ are checked in [RealDualSpec].
 */
class DualRingSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = DualAlgebras.dualRing(q)
    val withReciprocal = DualAlgebras.dualRingWithReciprocal(q)
    val normSq = DualAlgebras.normSq(q)
    val arb = ArbDual.rationalDual
    val arbQ = ArbDual.rational
    val pr = DualAlgebras.printableDual(
        RationalAlgebras.SignedRational, Rational.ZERO, Rational.ONE,
        RationalAlgebras.printableRational, RationalAlgebras.eqRational
    )

    val eps = DualAlgebras.infinitesimalUnit(q)
    val embed = DualAlgebras.scalarEmbedding(q)
    fun neg(x: Dual<Rational>) = ring.add.inverse(x)

    // ── Ring structure ───────────────────────────────────────────────────────

    "the dual numbers over ℚ satisfy CommutativeRingLaws and InvolutiveRingLaws" {
        CommutativeRingLaws(ring, arb, pr = pr).fullTest().throwIfFailed()
        InvolutiveRingLaws(ring, arb, pr = pr).fullTest().throwIfFailed()
    }

    "the dual numbers over ℤ satisfy CommutativeRingLaws and InvolutiveRingLaws" {
        val overZ = DualAlgebras.dualRing(IntegerAlgebras.IntegerCommutativeRing)
        CommutativeRingLaws(overZ, ArbDual.integerDual).fullTest().throwIfFailed()
        InvolutiveRingLaws(overZ, ArbDual.integerDual).fullTest().throwIfFailed()
    }

    "ε = 0 + 1ε is nonzero and squares to zero" {
        eps shouldBe dual(Rational.ZERO, Rational.ONE)
        (eps == ring.zero) shouldBe false
        ring.mul(eps, eps) shouldBe ring.zero
        ring.zero shouldBe dual(Rational.ZERO, Rational.ZERO)
        ring.one shouldBe dual(Rational.ONE, Rational.ZERO)
    }

    "component formulas: sum, negation, product and conjugate" {
        checkAll(arb, arb) { x, y ->
            ring.add(x, y) shouldBe dual(x.a + y.a, x.b + y.b)
            neg(x) shouldBe dual(-x.a, -x.b)
            ring.mul(x, y) shouldBe dual(x.a * y.a, x.a * y.b + x.b * y.a)
            ring.conj(x) shouldBe dual(x.a, -x.b)
        }
    }

    "accessors and destructuring agree: a/b, f/df and components" {
        checkAll(arb) { x ->
            val (a, b) = x
            (a to b) shouldBe (x.a to x.b)
            (x.f to x.df) shouldBe (x.a to x.b)
            Dual(x.cd) shouldBe x
        }
    }

    // ── Norm ─────────────────────────────────────────────────────────────────

    "normSq formula: N(a + bε) = a², and it is the scalar x·conj(x)" {
        checkAll(arb) { x ->
            normSq(x) shouldBe x.a * x.a
            ring.mul(x, ring.conj(x)) shouldBe embed(normSq(x))
        }
    }

    "normSq is exactly multiplicative, although degenerate: it ignores the tangent part" {
        NormSqMultiplicativeLaw(ring.mul.op, normSq, q.mul.op, arb, prA = pr).test()
        checkAll(arb, arbQ) { x, t ->
            normSq(dual(x.a, t)) shouldBe normSq(x)
        }
    }

    // ── Nilpotents, zero divisors and units ──────────────────────────────────

    "the pure infinitesimals form a square-zero ideal: (bε)(cε) = 0 and x·(bε) is pure" {
        checkAll(arbQ, arbQ, arb) { b, c, x ->
            ring.mul(dual(Rational.ZERO, b), dual(Rational.ZERO, c)) shouldBe ring.zero
            ring.mul(x, dual(Rational.ZERO, b)).a shouldBe Rational.ZERO
        }
    }

    "x is nilpotent exactly when its primal part is 0, and then x² = 0" {
        checkAll(arb) { x ->
            val square = ring.mul(x, x)
            (square == ring.zero) shouldBe (x.a == Rational.ZERO)
            square shouldBe dual(x.a * x.a, Rational.of(2) * x.a * x.b)
        }
    }

    "every nonzero pure infinitesimal is a zero divisor, and is not invertible" {
        checkAll(ArbDual.nonZeroRational) { b ->
            val x = dual(Rational.ZERO, b)
            ring.mul(x, eps) shouldBe ring.zero
            withReciprocal.hasReciprocal(x) shouldBe false
            shouldThrow<ArithmeticException> { withReciprocal.reciprocal(x) }
            withReciprocal.reciprocalOption(x) shouldBe Option.None
        }
        shouldThrow<ArithmeticException> { withReciprocal.reciprocal(ring.zero) }
    }

    "x is a unit exactly when its primal part is nonzero" {
        checkAll(arb) { x ->
            withReciprocal.hasReciprocal(x) shouldBe (x.a != Rational.ZERO)
        }
    }

    "reciprocal: (a + bε)⁻¹ = a⁻¹ - (b/a²)ε = conj(x)/N(x), and x·x⁻¹ = 1" {
        val action = DualAlgebras.dualLeftAction(q)
        checkAll(ArbDual.unitRationalDual) { x ->
            val inverse = withReciprocal.reciprocal(x)
            inverse shouldBe dual(x.a.reciprocal(), -x.b / (x.a * x.a))
            inverse shouldBe action(normSq(x).reciprocal(), ring.conj(x))
            ring.mul(x, inverse) shouldBe ring.one
            withReciprocal.reciprocal(inverse) shouldBe x
            withReciprocal.reciprocalOption(x) shouldBe Option.Some(inverse)
        }
    }

    "the reciprocal is multiplicative on units: (xy)⁻¹ = x⁻¹y⁻¹" {
        checkAll(ArbDual.unitRationalDual, ArbDual.unitRationalDual) { x, y ->
            withReciprocal.reciprocal(ring.mul(x, y)) shouldBe
                ring.mul(withReciprocal.reciprocal(x), withReciprocal.reciprocal(y))
        }
    }

    "binomial theorem truncates: (a + bε)ⁿ = aⁿ + n·aⁿ⁻¹·b·ε" {
        checkAll(arb, Arb.int(1..8)) { x, n ->
            ring.mul.pow(x, n) shouldBe dual(x.a.pow(n), n.toRational() * x.a.pow(n - 1) * x.b)
        }
        checkAll(arb) { x -> ring.mul.pow(x, 0) shouldBe ring.one }
    }

    // ── Module, algebra and star algebra ─────────────────────────────────────

    "the dual numbers over ℚ are a free ℚ-module satisfying RModuleLaws" {
        RModuleLaws(DualAlgebras.dualModule(q), arbQ, arb, prM = pr).fullTest().throwIfFailed()
    }

    "the dual numbers over ℚ satisfy AlgebraLaws and StarAlgebraLaws" {
        AlgebraLaws(DualAlgebras.dualAlgebra(q), arbQ, arb, prA = pr).fullTest().throwIfFailed()
        StarAlgebraLaws(DualAlgebras.dualStarAlgebra(q), arbQ, arb, prA = pr).fullTest().throwIfFailed()
    }

    "the scalar action is componentwise, and is multiplication by the embedded scalar" {
        val action = DualAlgebras.dualLeftAction(q)
        checkAll(arbQ, arb) { r, x ->
            action(r, x) shouldBe dual(r * x.a, r * x.b)
            action(r, x) shouldBe ring.mul(embed(r), x)
        }
    }

    // ── Morphisms ────────────────────────────────────────────────────────────

    "the scalar embedding ℚ ↪ Dual<ℚ>, r ↦ r + 0ε, is an injective unital ring homomorphism" {
        RingHomomorphismLaws(embed::invoke, q, ring, arbQ).fullTest().throwIfFailed()
        injectivityLaw(hom = embed::invoke, arbA = arbQ).test()
        checkAll(arbQ) { r ->
            embed(r) shouldBe dual(r, Rational.ZERO)
            embed(r) shouldBe DualAlgebras.constant(q, r)
        }
    }

    "the primal projection a + bε ↦ a is a unital ring homomorphism with kernel εℚ" {
        val projection = DualAlgebras.primalProjection(q)
        RingHomomorphismLaws(projection::invoke, ring, q, arb).fullTest().throwIfFailed()
        checkAll(arbQ, arb) { r, x ->
            projection(embed(r)) shouldBe r
            (projection(x) == Rational.ZERO) shouldBe (ring.mul(x, x) == ring.zero)
            projection(dual(Rational.ZERO, r)) shouldBe Rational.ZERO
        }
    }

    "the tangent part is not a homomorphism but a derivation: D(xy) = x₀·D(y) + D(x)·y₀" {
        checkAll(arb, arb) { x, y ->
            ring.mul(x, y).df shouldBe x.f * y.df + x.df * y.f
            ring.add(x, y).df shouldBe x.df + y.df
        }
    }

    "Dual<ℚ> ↪ M₂(ℚ), a + bε ↦ [[a, 0], [b, a]], is an injective unital ring homomorphism" {
        val mono = DualAlgebras.dualToRank2MatrixMonomorphism(q)
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = DenseMatAlgebras.DenseMatRing(q, 2),
            arb = arb
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = mono::invoke, arbA = arb).test()

        checkAll(arb) { x ->
            val m = mono(x)
            m shouldBe DenseMat.ofRows(listOf(listOf(x.a, Rational.ZERO), listOf(x.b, x.a)))
            // The determinant is the norm.
            m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0] shouldBe normSq(x)
        }
    }

    "the matrix of ε is nonzero and nilpotent" {
        val mono = DualAlgebras.dualToRank2MatrixMonomorphism(q)
        val matrices = DenseMatAlgebras.DenseMatRing(q, 2)
        val e = mono(eps)
        (e == matrices.zero) shouldBe false
        matrices.mul(e, e) shouldBe matrices.zero
    }

    // ── A finite field ───────────────────────────────────────────────────────

    "over 𝔽₅ the dual numbers are a ring of 25 elements with 20 units and 5 nilpotents" {
        val f5 = PrimeField(BigInteger.valueOf(5))
        val duals = DualAlgebras.dualRingWithReciprocal(f5)
        val residues = (0..4).map(Int::toBigInteger)
        val all = residues.flatMap { a -> residues.map { b -> dual(a, b) } }
        all.toSet().size shouldBe 25

        CommutativeRingLaws(duals, ArbDual.dualOf(Arb.element(residues))).fullTest().throwIfFailed()

        val units = all.filter { x -> duals.hasReciprocal(x) }
        units.size shouldBe 20
        units.forEach { x -> duals.mul(x, duals.reciprocal(x)) shouldBe duals.one }

        val nilpotents = all.filter { x -> duals.mul(x, x) == duals.zero }
        nilpotents.size shouldBe 5
        (all - units.toSet()).toSet() shouldBe nilpotents.toSet()
        nilpotents.forEach { x -> shouldThrow<ArithmeticException> { duals.reciprocal(x) } }
    }

    // ── Printing ─────────────────────────────────────────────────────────────

    "printable renders a + bε over the basis {1, ε}, dropping zero terms" {
        pr(ring.zero) shouldBe "0"
        pr(ring.one) shouldBe "1"
        pr(eps) shouldBe Symbols.EPSILON
        pr(dual(Rational.of(1, 2), Rational.of(-3))).isNotEmpty() shouldBe true
    }
})
