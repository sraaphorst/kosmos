package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Forward-mode automatic differentiation with dual numbers.
 *
 * Over ℚ the rules of differentiation are checked *exactly*, as identities of the dual ring: no
 * limits and no tolerances are involved. Over ℝ, [DualAlgebras.diffAtReal] is checked against the
 * closed-form derivatives of compositions of the elementary functions in [DualRealFns].
 */
class DualDifferentiationSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = DualAlgebras.dualRingWithReciprocal(q)
    val arbQ = ArbDual.rational

    fun const(r: Rational): Dual<Rational> = DualAlgebras.constant(q, r)
    fun const(n: Int): Dual<Rational> = const(Rational.of(n))
    fun plus(x: Dual<Rational>, y: Dual<Rational>) = ring.add(x, y)
    fun times(x: Dual<Rational>, y: Dual<Rational>) = ring.mul(x, y)
    fun minus(x: Dual<Rational>, y: Dual<Rational>) = ring.add(x, ring.add.inverse(y))

    /** p(x) = 3x³ - 2x + 5, with p'(x) = 9x² - 2. */
    val p = Endo<Dual<Rational>> { x ->
        plus(minus(times(const(3), ring.mul.pow(x, 3)), times(const(2), x)), const(5))
    }

    /** g(x) = x² + 1, with g'(x) = 2x; never zero over ℚ. */
    val g = Endo<Dual<Rational>> { x -> plus(times(x, x), const(1)) }

    "seeds: a variable has derivative 1 and a constant has derivative 0" {
        checkAll(arbQ) { a ->
            DualAlgebras.variable(q, a) shouldBe dual(a, Rational.ONE)
            DualAlgebras.constant(q, a) shouldBe dual(a, Rational.ZERO)
            DualAlgebras.diffAt(q, Endo { x -> x }, a) shouldBe (a to Rational.ONE)
            DualAlgebras.diffAt(q, Endo { _ -> const(a) }, a) shouldBe (a to Rational.ZERO)
        }
    }

    "diffAt returns (f(x), f'(x)) in that order, exactly, for a polynomial" {
        checkAll(arbQ) { a ->
            val expectedValue = Rational.of(3) * a * a * a - Rational.of(2) * a + Rational.of(5)
            val expectedDerivative = Rational.of(9) * a * a - Rational.of(2)
            DualAlgebras.diffAt(q, p, a) shouldBe (expectedValue to expectedDerivative)
        }
    }

    "power rule: d/dx xⁿ = n·xⁿ⁻¹" {
        checkAll(arbQ, Arb.int(1..9)) { a, n ->
            DualAlgebras.diffAt(q, Endo { x -> ring.mul.pow(x, n) }, a) shouldBe
                (a.pow(n) to n.toRational() * a.pow(n - 1))
        }
    }

    "linearity: (αf + βg)' = αf' + βg'" {
        checkAll(arbQ, arbQ, arbQ) { a, alpha, beta ->
            val (_, dp) = DualAlgebras.diffAt(q, p, a)
            val (_, dg) = DualAlgebras.diffAt(q, g, a)
            val combined = Endo<Dual<Rational>> { x -> plus(times(const(alpha), p(x)), times(const(beta), g(x))) }
            DualAlgebras.diffAt(q, combined, a).second shouldBe alpha * dp + beta * dg
        }
    }

    "product rule: (fg)' = f'g + fg'" {
        checkAll(arbQ) { a ->
            val (pv, dp) = DualAlgebras.diffAt(q, p, a)
            val (gv, dg) = DualAlgebras.diffAt(q, g, a)
            DualAlgebras.diffAt(q, Endo { x -> times(p(x), g(x)) }, a) shouldBe
                (pv * gv to dp * gv + pv * dg)
        }
    }

    "quotient rule: (f/g)' = (f'g - fg')/g²" {
        checkAll(arbQ) { a ->
            val (pv, dp) = DualAlgebras.diffAt(q, p, a)
            val (gv, dg) = DualAlgebras.diffAt(q, g, a)
            DualAlgebras.diffAt(q, Endo { x -> times(p(x), ring.reciprocal(g(x))) }, a) shouldBe
                (pv / gv to (dp * gv - pv * dg) / (gv * gv))
        }
    }

    "reciprocal rule: (1/g)' = -g'/g²" {
        checkAll(arbQ) { a ->
            val (gv, dg) = DualAlgebras.diffAt(q, g, a)
            DualAlgebras.diffAt(q, Endo { x -> ring.reciprocal(g(x)) }, a) shouldBe
                (gv.reciprocal() to -dg / (gv * gv))
        }
    }

    "chain rule: (f ∘ g)'(x) = f'(g(x))·g'(x)" {
        checkAll(arbQ) { a ->
            val (gv, dg) = DualAlgebras.diffAt(q, g, a)
            val (pAtG, dpAtG) = DualAlgebras.diffAt(q, p, gv)
            DualAlgebras.diffAt(q, Endo { x -> p(g(x)) }, a) shouldBe (pAtG to dpAtG * dg)
        }
    }

    "a function evaluated at a + bε carries b·f'(a): the directional derivative" {
        checkAll(arbQ, arbQ) { a, b ->
            val (value, derivative) = DualAlgebras.diffAt(q, p, a)
            p(dual(a, b)) shouldBe dual(value, b * derivative)
        }
    }

    "differentiation works over any commutative ring: polynomials over ℤ" {
        val z = IntegerAlgebras.IntegerCommutativeRing
        val zRing = DualAlgebras.dualRing(z)
        checkAll(Arb.int(-20..20), Arb.int(1..6)) { a, n ->
            val big = a.toBigInteger()
            DualAlgebras.diffAt(z, Endo { x -> zRing.mul.pow(x, n) }, big) shouldBe
                (big.pow(n) to n.toBigInteger() * big.pow(n - 1))
        }
    }

    "nested duals carry the second derivative: Dual<Dual<ℚ>> evaluates (f, f', f', f'')" {
        // Over the ring Dual(ℚ), seed the point (a + 1·ε₁) with outer tangent 1.
        val inner = DualAlgebras.dualRing(q)
        val outer = DualAlgebras.dualRing(inner)
        fun lift(n: Int): Dual<Dual<Rational>> =
            DualAlgebras.constant(inner, DualAlgebras.constant(q, Rational.of(n)))

        checkAll(arbQ) { a ->
            val seed = DualAlgebras.variable(inner, DualAlgebras.variable(q, a))
            // p(x) = 3x³ - 2x + 5, p'(x) = 9x² - 2, p''(x) = 18x.
            val y = outer.add(
                outer.add(
                    outer.mul(lift(3), outer.mul.pow(seed, 3)),
                    outer.add.inverse(outer.mul(lift(2), seed))
                ),
                lift(5)
            )
            val value = Rational.of(3) * a * a * a - Rational.of(2) * a + Rational.of(5)
            val first = Rational.of(9) * a * a - Rational.of(2)
            val second = Rational.of(18) * a

            y shouldBe dual(dual(value, first), dual(first, second))
        }
    }

    "diffAtReal differentiates compositions of elementary functions" {
        val eq = RealAlgebras.eqRealApprox
        checkAll(Arb.double(-3.0..3.0)) { a ->
            // exp(sin(x)) has derivative cos(x)·exp(sin(x)).
            val (v1, d1) = DualAlgebras.diffAtReal(Endo { x -> DualRealFns.exp(DualRealFns.sin(x)) }, a)
            check(eq(v1, exp(sin(a))))
            check(eq(d1, cos(a) * exp(sin(a))))

            // log(exp(x)) is the identity, with derivative 1.
            val (v2, d2) = DualAlgebras.diffAtReal(Endo { x -> DualRealFns.log(DualRealFns.exp(x)) }, a)
            check(eq(v2, a))
            check(eq(d2, 1.0))

            // x·sin(x) by the product rule in the ring.
            val ring2 = DualAlgebras.RealDualRing
            val (v3, d3) = DualAlgebras.diffAtReal(Endo { x -> ring2.mul(x, DualRealFns.sin(x)) }, a)
            check(eq(v3, a * sin(a)))
            check(eq(d3, sin(a) + a * cos(a)))
        }
    }
})
