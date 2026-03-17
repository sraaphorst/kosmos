package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras.dual
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero

class DualRingPropertySpec : FunSpec({

    val dualRing = RealAlgebras.RealField.dual {
        RealAlgebras.eqRealApprox(it, 0.0)
    }
    val zero = dualRing.add.identity
    val one = dualRing.mul.identity

    val dualPr = DualAlgebras.printableDualRealSigned
    val dualPrCompact = DualAlgebras.printableDualRealCompactSigned

    fun dual(a: Real, b: Real): Dual<Real> =
        Dual(a, b)

    operator fun Dual<Real>.plus(other: Dual<Real>): Dual<Real> =
        dualRing.add(this, other)

    operator fun Dual<Real>.unaryMinus(): Dual<Real> =
        dualRing.add.inverse(this)

    operator fun Dual<Real>.minus(other: Dual<Real>): Dual<Real> =
        this + (-other)

    operator fun Dual<Real>.times(other: Dual<Real>): Dual<Real> =
        dualRing.mul(this, other)

    fun Dual<Real>.reciprocal(): Dual<Real> =
        dualRing.reciprocal(this)

    operator fun Dual<Real>.div(other: Dual<Real>): Dual<Real> =
        this * other.reciprocal()

    fun Dual<Real>.isInvertible(): Boolean =
        dualRing.hasReciprocal(this)


    fun <A : Any> Option<A>.expectSome(): A =
        when (this) {
            is Option.Some -> value
            Option.None -> error("Expected Option.Some, but got None")
        }

    context("Dual number construction") {

        test("dual number stores components correctly") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                d.a shouldBe a
                d.b shouldBe b
            }
        }

        test("component destructuring works") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                val (c1, c2) = d
                c1 shouldBe a
                c2 shouldBe b
            }
        }

        test("lift creates dual number with zero infinitesimal part") {
            checkAll(arbDualReal()) { a ->
                val d = dualRing.lift(a)
                d.a shouldBe a
                d.b shouldBe 0.0
            }
        }

        test("eps creates dual number with zero real part") {
            checkAll(arbDualReal()) { b ->
                val d = dualRing.eps(b)
                d.a shouldBe 0.0
                d.b shouldBe b
            }
        }

        test("canonical epsilon element: epsOne = 0 + 1ε") {
            val e = dualRing.epsOne
            e.a shouldBe 0.0
            e.b shouldBe 1.0
        }
    }

    context("Addition - Abelian Group") {

        test("addition is commutative: d1 + d2 = d2 + d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                (d1 + d2) shouldBeApproximately (d2 + d1)
            }
        }

        test("addition is associative: (d1 + d2) + d3 = d1 + (d2 + d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                ((d1 + d2) + d3) shouldBeApproximately (d1 + (d2 + d3))
            }
        }

        test("additive identity: d + 0 = d") {
            checkAll(arbDual()) { d ->
                (d + zero) shouldBeApproximately d
            }
        }

        test("additive identity is 0 + 0ε") {
            zero.a shouldBe 0.0
            zero.b shouldBe 0.0
        }

        test("additive inverse: d + (-d) = 0") {
            checkAll(arbDual()) { d ->
                (d + (-d)) shouldBeApproximately zero
            }
        }

        test("negation negates both components") {
            checkAll(arbDual()) { d ->
                val neg = -d
                neg.a shouldBeApproximately -d.a
                neg.b shouldBeApproximately -d.b
            }
        }

        test("double negation: -(-d) = d") {
            checkAll(arbDual()) { d ->
                (-(-d)) shouldBeApproximately d
            }
        }
    }

    context("Multiplication - Monoid") {

        test("multiplication is associative: (d1 * d2) * d3 = d1 * (d2 * d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                ((d1 * d2) * d3) shouldBeApproximately (d1 * (d2 * d3))
            }
        }

        test("multiplication is commutative: d1 * d2 = d2 * d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                (d1 * d2) shouldBeApproximately (d2 * d1)
            }
        }

        test("multiplicative identity: d * 1 = d") {
            checkAll(arbDual()) { d ->
                (d * one) shouldBeApproximately d
            }
        }

        test("multiplicative identity is 1 + 0ε") {
            one.a shouldBe 1.0
            one.b shouldBe 0.0
        }

        test("multiplication formula: (a+bε)(c+dε) = ac + (ad+bc)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c, d ->
                val product = dual(a, b) * dual(c, d)
                product.a shouldBeApproximately (a * c)
                product.b shouldBeApproximately (a * d + b * c)
            }
        }
    }

    context("Ring distributivity") {

        test("left distributivity: d1 * (d2 + d3) = d1*d2 + d1*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                (d1 * (d2 + d3)) shouldBeApproximately ((d1 * d2) + (d1 * d3))
            }
        }

        test("right distributivity: (d1 + d2) * d3 = d1*d3 + d2*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                ((d1 + d2) * d3) shouldBeApproximately ((d1 * d3) + (d2 * d3))
            }
        }

        test("multiplication by zero gives zero: d * 0 = 0") {
            checkAll(arbDual()) { d ->
                (d * zero) shouldBeApproximately zero
            }
        }
    }

    context("Epsilon nilpotency") {

        test("epsilon squared is zero: ε² = 0") {
            (dualRing.epsOne * dualRing.epsOne) shouldBeApproximately zero
        }

        test("pure infinitesimal squared is zero: (0+bε)² = 0") {
            checkAll(arbDualReal()) { b ->
                (dualRing.eps(b) * dualRing.eps(b)) shouldBeApproximately zero
            }
        }
    }

    context("Lift operation") {

        test("lift preserves addition: lift(a + b) = lift(a) + lift(b)") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                dualRing.lift(a + b) shouldBeApproximately (dualRing.lift(a) + dualRing.lift(b))
            }
        }

        test("lift preserves multiplication: lift(a * b) = lift(a) * lift(b)") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                dualRing.lift(a * b) shouldBeApproximately (dualRing.lift(a) * dualRing.lift(b))
            }
        }

        test("lift preserves identity: lift(1) = 1 + 0ε") {
            dualRing.lift(1.0) shouldBeApproximately one
        }

        test("lifted elements commute with epsilon") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val lifted = dualRing.lift(a)
                val eps = dualRing.eps(b)
                (lifted * eps) shouldBeApproximately (eps * lifted)
            }
        }
    }

    context("Subtraction") {

        test("self-subtraction gives zero: d - d = 0") {
            checkAll(arbDual()) { d ->
                (d - d) shouldBeApproximately zero
            }
        }

        test("subtraction components: (a₁+b₁ε) - (a₂+b₂ε) = (a₁-a₂) + (b₁-b₂)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal(), arbDualReal()) { a1, b1, a2, b2 ->
                val diff = dual(a1, b1) - dual(a2, b2)
                diff.a shouldBeApproximately (a1 - a2)
                diff.b shouldBeApproximately (b1 - b2)
            }
        }
    }

    context("Invertibility") {

        test("dual number is invertible iff real part is non-zero") {
            checkAll(arbDual()) { d ->
                d.isInvertible() shouldBe (RealAlgebras.eqRealApprox.neqv(d.a, 0.0) && d.a.isFinite())
//                d.isInvertible() shouldBe (d.a != 0.0)
            }
        }

        test("lifted non-zero elements are invertible") {
            checkAll(arbNonZeroDualReal()) { a ->
                dualRing.lift(a).isInvertible() shouldBe true
            }
        }

        test("pure infinitesimals are not invertible") {
            checkAll(arbNonZeroDualReal()) { b ->
                dualRing.eps(b).isInvertible() shouldBe false
            }
        }

        test("zero is not invertible") {
            zero.isInvertible() shouldBe false
        }
    }

    context("Reciprocal") {

        test("reciprocal of invertible dual: d * d⁻¹ = 1") {
            checkAll(arbInvertibleDual()) { d ->
                (d * d.reciprocal()) shouldBeApproximately one
            }
        }

        test("reciprocal formula: (a+bε)⁻¹ = a⁻¹ - ba⁻²ε") {
            checkAll(arbNonZeroDualReal(), arbDualReal()) { a, b ->
                val inv = dual(a, b).reciprocal()

                inv.a shouldBeApproximately (1.0 / a)
                inv.b shouldBeApproximately (-b / (a * a))
            }
        }

        test("reciprocalOption returns None for non-invertible elements") {
            checkAll(arbNonInvertibleDual()) { d ->
                dualRing.reciprocalOption(d) shouldBe Option.None
            }
        }

        test("reciprocalOption returns Some for invertible elements") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = dualRing.reciprocalOption(d).expectSome()
                (d * inv) shouldBeApproximately one
            }
        }

        test("double reciprocal: (d⁻¹)⁻¹ = d") {
            checkAll(arbInvertibleDual()) { d ->
                d.reciprocal().reciprocal() shouldBeApproximately d
            }
        }
    }

    context("Division") {

        test("division by one is identity: d / 1 = d") {
            checkAll(arbDual()) { d ->
                (d / one) shouldBeApproximately d
            }
        }

        test("self-division gives one: d / d = 1") {
            checkAll(arbInvertibleDual()) { d ->
                (d / d) shouldBeApproximately one
            }
        }

        test("division is multiplication by reciprocal: d1 / d2 = d1 * d2⁻¹") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val d2Inv = dualRing.reciprocalOption(d2).expectSome()
                (d1 / d2) shouldBeApproximately (d1 * d2Inv)
            }
        }

        test("reciprocalOption returns None for non-invertible divisors") {
            checkAll(arbNonInvertibleDual()) { d ->
                dualRing.reciprocalOption(d) shouldBe Option.None
            }
        }
    }

    context("Scalar operations") {

        test("dual plus scalar: (a+bε) + c = (a+c) + bε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val result = dual(a, b) + dualRing.lift(c)
                result.a shouldBeApproximately (a + c)
                result.b shouldBeApproximately b
            }
        }

        test("dual minus scalar: (a+bε) - c = (a-c) + bε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val result = dual(a, b) - dualRing.lift(c)
                result.a shouldBeApproximately (a - c)
                result.b shouldBeApproximately b
            }
        }

        test("dual times scalar: (a+bε) * c = (ac) + (bc)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val result = dual(a, b) * dualRing.lift(c)
                result.a shouldBeApproximately (a * c)
                result.b shouldBeApproximately (b * c)
            }
        }

        test("dual divided by scalar: (a+bε) / c = (a/c) + (b/c)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbNonZeroDualReal()) { a, b, c ->
                val result = dual(a, b) / dualRing.lift(c)
                result.a shouldBeApproximately (a / c)
                result.b shouldBeApproximately (b / c)
            }
        }

        test("zero scalar has no reciprocal") {
            dualRing.reciprocalOption(dualRing.lift(0.0)) shouldBe Option.None
        }
    }

    context("Complex expressions") {

        test("polynomial evaluation: (a+bε)² = a² + 2abε") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d2 = dual(a, b) * dual(a, b)
                d2.a shouldBeApproximately (a * a)
                d2.b shouldBeApproximately (2.0 * a * b)
            }
        }

        test("polynomial: (a+bε)³ = a³ + 3a²bε") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                val d3 = d * d * d
                d3.a shouldBeApproximately (a * a * a)
                d3.b shouldBeApproximately (3.0 * a * a * b)
            }
        }

        test("sum of products: d1*d2 + d3*d4") {
            checkAll(arbDual(), arbDual(), arbDual(), arbDual()) { d1, d2, d3, d4 ->
                val sum = d1 * d2 + d3 * d4

                val expectedA = d1.a * d2.a + d3.a * d4.a
                val expectedB = d1.a * d2.b + d1.b * d2.a + d3.a * d4.b + d3.b * d4.a

                sum.a shouldBeApproximately expectedA
                sum.b shouldBeApproximately expectedB
            }
        }

        test("mixed operations: (d1 + 5.0) * (d2 - 3.0)") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val result = (d1 + dualRing.lift(5.0)) * (d2 - dualRing.lift(3.0))

                val expectedLeft = dual(d1.a + 5.0, d1.b)
                val expectedRight = dual(d2.a - 3.0, d2.b)
                val expected = expectedLeft * expectedRight

                result shouldBeApproximately expected
            }
        }
    }

    context("Automatic differentiation") {

        test("linear function derivative: f(x) = mx + b, f'(x) = m") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { x, m, b ->
                val input = dual(x, 1.0)
                val result = dualRing.lift(m) * input + dualRing.lift(b)

                result.a shouldBeApproximately (m * x + b)
                result.b shouldBeApproximately m
            }
        }

        test("quadratic function derivative: f(x) = x², f'(x) = 2x") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0)
                val result = input * input

                result.a shouldBeApproximately (x * x)
                result.b shouldBeApproximately (2.0 * x)
            }
        }

        test("cubic function derivative: f(x) = x³, f'(x) = 3x²") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0)
                val result = input * input * input

                result.a shouldBeApproximately (x * x * x)
                result.b shouldBeApproximately (3.0 * x * x)
            }
        }

        test("product rule: (fg)' = f'g + fg'") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal(), arbDualReal()) { _, f0, g0, fPrime ->
                val f = dual(f0, fPrime)
                val g = dual(g0, 1.0)

                val product = f * g
                val expectedDerivative = fPrime * g0 + f0

                product.b shouldBeApproximately expectedDerivative
            }
        }

        test("chain rule through composition") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0)
                val u = dualRing.lift(3.0) * input
                val result = u * u

                result.a shouldBeApproximately (9.0 * x * x)
                result.b shouldBeApproximately (18.0 * x)
            }
        }
    }

    context("Edge cases") {

        test("operations on zero") {
            (zero + zero) shouldBeApproximately zero
            (zero * zero) shouldBeApproximately zero
            (zero * one) shouldBeApproximately zero
            (one * zero) shouldBeApproximately zero
        }

        test("operations on epsilon") {
            val e = dualRing.epsOne
            val twoE = e + e

            twoE.a.shouldBeZero()
            twoE.b shouldBeApproximately 2.0

            (e * e) shouldBeApproximately zero
        }

        test("very small infinitesimal parts") {
            checkAll(arbDualReal()) { a ->
                val d = dual(a, 1e-10)
                val d2 = d * d

                d2.a shouldBeApproximately (a * a)
                d2.b shouldBeApproximately (2.0 * a * 1e-10)
            }
        }
    }

    context("String representations") {
        val eps = Symbols.EPSILON

        test("Printable format, positive") {
            val d = dual(3.0, 4.0)
            dualPr(d) shouldBe "3.0 + 4.0$eps"
            dualPrCompact(d) shouldBe "3.0 + 4.0$eps"
        }

        test("Printable format, negative") {
            val d = dual(-3.0, -4.0)
            dualPr(d) shouldBe "-3.0 - 4.0$eps"
            dualPrCompact(d) shouldBe "-3.0 - 4.0$eps"
        }

        test("zero dual toString") {
            dualPr(zero) shouldBe "0.0 + 0.0$eps"
            dualPrCompact(zero) shouldBe "0.0"
        }

        test("one dual toString") {
            dualPr(one) shouldBe "1.0 + 0.0ε"
            dualPrCompact(one) shouldBe "1.0"
        }

        test("epsilon toString") {
            val e = dualRing.epsOne
            dualPr(e) shouldBe "0.0 + 1.0ε"
            dualPrCompact(e) shouldBe eps
        }
    }
})
