package org.vorpal.kosmos.algebra.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero


class DualRingPropertyTest : FunSpec({

    val dualRing = RealField.dual()

    // Helper to create dual numbers easily in tests
    fun dual(a: Double, b: Double) = dualRing.Dual(a, b)

    // ------------------------------------------------------------------------
    // Basic Structure
    // ------------------------------------------------------------------------

    context("Dual number construction") {

        test("dual number stores components correctly") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val d = dual(a, b)
                d.a shouldBe a
                d.b shouldBe b
            }
        }

        test("component destructuring works") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val d = dual(a, b)
                val (c1, c2) = d
                c1 shouldBe a
                c2 shouldBe b
            }
        }

        test("lift creates dual number with zero infinitesimal part") {
            checkAll(arbDualDouble()) { a ->
                val d = dualRing.lift(a)
                d.a shouldBe a
                d.b shouldBe 0.0
            }
        }

        test("eps creates dual number with zero real part") {
            checkAll(arbDualDouble()) { b ->
                val d = dualRing.eps(b)
                d.a shouldBe 0.0
                d.b shouldBe b
            }
        }

        test("canonical epsilon element: e = 0 + 1ε") {
            val e = dualRing.e
            e.a shouldBe 0.0
            e.b shouldBe 1.0
        }
    }

    // ------------------------------------------------------------------------
    // Addition (Abelian Group Laws)
    // ------------------------------------------------------------------------

    context("Addition - Abelian Group") {

        test("addition is commutative: d1 + d2 = d2 + d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = d1 + d2
                val right = d2 + d1
                left shouldBeApproximately right
            }
        }

        test("addition is associative: (d1 + d2) + d3 = d1 + (d2 + d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = (d1 + d2) + d3
                val right = d1 + (d2 + d3)
                left shouldBeApproximately right
            }
        }

        test("additive identity: d + 0 = d") {
            checkAll(arbDual()) { d ->
                val zero = dualRing.add.identity
                val result = d + zero
                result shouldBeApproximately d
            }
        }

        test("additive identity is 0 + 0ε") {
            val zero = dualRing.add.identity
            zero.a shouldBe 0.0
            zero.b shouldBe 0.0
        }

        test("additive inverse: d + (-d) = 0") {
            checkAll(arbDual()) { d ->
                val neg = -d
                val sum = d + neg
                val zero = dualRing.add.identity
                sum shouldBeApproximately zero
            }
        }

        test("unary minus negates both components") {
            checkAll(arbDual()) { d ->
                val neg = -d
                neg.a shouldBeApproximately -d.a
                neg.b shouldBeApproximately -d.b
            }
        }

        test("double negation is identity: -(-d) = d") {
            checkAll(arbDual()) { d ->
                val result = -(-d)
                result shouldBeApproximately d
            }
        }

        test("addition components: (a₁+b₁ε) + (a₂+b₂ε) = (a₁+a₂) + (b₁+b₂)ε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble(), arbDualDouble()) { a1, b1, a2, b2 ->
                val d1 = dual(a1, b1)
                val d2 = dual(a2, b2)
                val sum = d1 + d2

                sum.a shouldBeApproximately (a1 + a2)
                sum.b shouldBeApproximately (b1 + b2)
            }
        }

        test("addition via ring operation matches operator") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val viaOperator = d1 + d2
                val viaRing = dualRing.add.op(d1, d2)
                viaOperator shouldBeApproximately viaRing
            }
        }
    }

    // ------------------------------------------------------------------------
    // Multiplication (Monoid Laws)
    // ------------------------------------------------------------------------

    context("Multiplication - Monoid") {

        test("multiplication is associative: (d1 * d2) * d3 = d1 * (d2 * d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = (d1 * d2) * d3
                val right = d1 * (d2 * d3)
                left shouldBeApproximately right
            }
        }

        test("multiplication is commutative: d1 * d2 = d2 * d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = d1 * d2
                val right = d2 * d1
                left shouldBeApproximately right
            }
        }

        test("multiplicative identity: d * 1 = d") {
            checkAll(arbDual()) { d ->
                val one = dualRing.mul.identity
                val result = d * one
                result shouldBeApproximately d
            }
        }

        test("multiplicative identity is 1 + 0ε") {
            val one = dualRing.mul.identity
            one.a shouldBe 1.0
            one.b shouldBe 0.0
        }

        test("multiplication formula: (a+bε)(c+dε) = ac + (ad+bc)ε") {
            checkAll(
                arbDualDouble(), arbDualDouble(),
                arbDualDouble(), arbDualDouble()
            ) { a, b, c, d ->
                val d1 = dual(a, b)
                val d2 = dual(c, d)
                val product = d1 * d2

                val expectedA = a * c
                val expectedB = a * d + b * c

                product.a shouldBeApproximately expectedA
                product.b shouldBeApproximately expectedB
            }
        }

        test("multiplication via ring operation matches operator") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val viaOperator = d1 * d2
                val viaRing = dualRing.mul.op(d1, d2)
                viaOperator shouldBeApproximately viaRing
            }
        }
    }

    // ------------------------------------------------------------------------
    // Distributivity (Ring Laws)
    // ------------------------------------------------------------------------

    context("Ring distributivity") {

        test("left distributivity: d1 * (d2 + d3) = d1*d2 + d1*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = d1 * (d2 + d3)
                val right = (d1 * d2) + (d1 * d3)
                left shouldBeApproximately right
            }
        }

        test("right distributivity: (d1 + d2) * d3 = d1*d3 + d2*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = (d1 + d2) * d3
                val right = (d1 * d3) + (d2 * d3)
                left shouldBeApproximately right
            }
        }

        test("multiplication by zero gives zero: d * 0 = 0") {
            checkAll(arbDual()) { d ->
                val zero = dualRing.add.identity
                val result = d * zero
                result shouldBeApproximately zero
            }
        }
    }

    // ------------------------------------------------------------------------
    // Epsilon Properties (ε² = 0)
    // ------------------------------------------------------------------------

    context("Epsilon nilpotency") {

        test("epsilon squared is zero: ε² = 0") {
            val e = dualRing.e
            val e2 = e * e
            val zero = dualRing.add.identity
            e2 shouldBeApproximately zero
        }

        test("pure infinitesimal squared is zero: (0+bε)² = 0") {
            checkAll(arbDualDouble()) { b ->
                val d = dualRing.eps(b)
                val d2 = d * d
                val zero = dualRing.add.identity
                d2 shouldBeApproximately zero
            }
        }

        test("epsilon times epsilon is zero: ε * ε = 0") {
            val e = dualRing.e
            val product = e * e
            product.a.shouldBeZero()
            product.b.shouldBeZero()
        }

        test("any power of epsilon beyond 1 is zero") {
            checkAll(arbDualDouble()) { b ->
                val eps = dualRing.eps(b)
                val eps2 = eps * eps
                val eps3 = eps2 * eps

                val zero = dualRing.add.identity
                eps2 shouldBeApproximately zero
                eps3 shouldBeApproximately zero
            }
        }
    }

    // ------------------------------------------------------------------------
    // Lift and Injection
    // ------------------------------------------------------------------------

    context("Lift operation") {

        test("lift preserves addition: lift(a + b) = lift(a) + lift(b)") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val left = dualRing.lift(a + b)
                val right = dualRing.lift(a) + dualRing.lift(b)
                left shouldBeApproximately right
            }
        }

        test("lift preserves multiplication: lift(a * b) = lift(a) * lift(b)") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val left = dualRing.lift(a * b)
                val right = dualRing.lift(a) * dualRing.lift(b)
                left shouldBeApproximately right
            }
        }

        test("lift preserves identity: lift(1) = 1 + 0ε") {
            val lifted = dualRing.lift(1.0)
            val one = dualRing.mul.identity
            lifted shouldBeApproximately one
        }

        test("lift preserves zero: lift(0) = 0 + 0ε") {
            val lifted = dualRing.lift(0.0)
            val zero = dualRing.add.identity
            lifted shouldBeApproximately zero
        }

        test("lifted elements commute with epsilon") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val lifted = dualRing.lift(a)
                val eps = dualRing.eps(b)

                val left = lifted * eps
                val right = eps * lifted
                left shouldBeApproximately right
            }
        }
    }

    // ------------------------------------------------------------------------
    // Subtraction
    // ------------------------------------------------------------------------

    context("Subtraction") {

        test("subtraction is addition of inverse: d1 - d2 = d1 + (-d2)") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = d1 - d2
                val right = d1 + (-d2)
                left shouldBeApproximately right
            }
        }

        test("self-subtraction gives zero: d - d = 0") {
            checkAll(arbDual()) { d ->
                val result = d - d
                val zero = dualRing.add.identity
                result shouldBeApproximately zero
            }
        }

        test("subtraction components: (a₁+b₁ε) - (a₂+b₂ε) = (a₁-a₂) + (b₁-b₂)ε") {
            checkAll(
                arbDualDouble(), arbDualDouble(),
                arbDualDouble(), arbDualDouble()
            ) { a1, b1, a2, b2 ->
                val d1 = dual(a1, b1)
                val d2 = dual(a2, b2)
                val diff = d1 - d2

                diff.a shouldBeApproximately (a1 - a2)
                diff.b shouldBeApproximately (b1 - b2)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Invertibility
    // ------------------------------------------------------------------------

    context("Invertibility") {

        test("dual number is invertible iff real part is non-zero") {
            checkAll(arbDual()) { d ->
                val isInvertible = d.isInvertible()
                val expectedInvertible = d.a != 0.0
                isInvertible shouldBe expectedInvertible
            }
        }

        test("lifted non-zero elements are invertible") {
            checkAll(arbNonZeroDualDouble()) { a ->
                val d = dualRing.lift(a)
                d.isInvertible() shouldBe true
            }
        }

        test("pure infinitesimals are not invertible") {
            checkAll(arbNonZeroDualDouble()) { b ->
                val d = dualRing.eps(b)
                d.isInvertible() shouldBe false
            }
        }

        test("zero is not invertible") {
            val zero = dualRing.add.identity
            zero.isInvertible() shouldBe false
        }

        test("multiplicative identity is invertible") {
            val one = dualRing.mul.identity
            one.isInvertible() shouldBe true
        }
    }

    // ------------------------------------------------------------------------
    // Reciprocal
    // ------------------------------------------------------------------------

    context("Reciprocal") {

        test("reciprocal of invertible dual: d * d⁻¹ = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = d.reciprocal()
                val product = d * inv
                val one = dualRing.mul.identity
                product shouldBeApproximately one
            }
        }

        test("reciprocal formula: (a+bε)⁻¹ = a⁻¹ - ba⁻²ε") {
            checkAll(arbNonZeroDualDouble(), arbDualDouble()) { a, b ->
                val d = dual(a, b)
                val inv = d.reciprocal()

                val expectedA = 1.0 / a
                val expectedB = -b / (a * a)

                inv.a shouldBeApproximately expectedA
                inv.b shouldBeApproximately expectedB
            }
        }

        test("reciprocal is two-sided inverse: d⁻¹ * d = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = d.reciprocal()
                val product = inv * d
                val one = dualRing.mul.identity
                product shouldBeApproximately one
            }
        }

        test("reciprocal of lifted element: lift(a)⁻¹ = lift(a⁻¹)") {
            checkAll(arbNonZeroDualDouble()) { a ->
                val d = dualRing.lift(a)
                val inv = d.reciprocal()
                val expected = dualRing.lift(1.0 / a)
                inv shouldBeApproximately expected
            }
        }

        test("reciprocalOrNull returns null for non-invertible") {
            checkAll(arbNonInvertibleDual()) { d ->
                d.reciprocalOrNull() shouldBe null
            }
        }

        test("reciprocalOrNull returns reciprocal for invertible") {
            checkAll(arbInvertibleDual()) { d ->
                val invOrNull = d.reciprocalOrNull()
                invOrNull shouldNotBe null

                val product = d * invOrNull!!
                val one = dualRing.mul.identity
                product shouldBeApproximately one
            }
        }

        test("double reciprocal is identity: (d⁻¹)⁻¹ = d") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = d.reciprocal()
                val invInv = inv.reciprocal()
                invInv shouldBeApproximately d
            }
        }
    }

    // ------------------------------------------------------------------------
    // Division
    // ------------------------------------------------------------------------

    context("Division") {

        test("division by one is identity: d / 1 = d") {
            checkAll(arbDual()) { d ->
                val one = dualRing.mul.identity
                val result = d / one
                result shouldBeApproximately d
            }
        }

        test("self-division gives one: d / d = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val result = d / d
                val one = dualRing.mul.identity
                result shouldBeApproximately one
            }
        }

        test("division is multiplication by reciprocal: d1 / d2 = d1 * d2⁻¹") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val left = d1 / d2
                val right = d1 * d2.reciprocal()
                left shouldBeApproximately right
            }
        }

        test("division then multiplication recovers original: (d1 / d2) * d2 = d1") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val divided = d1 / d2
                val recovered = divided * d2
                recovered shouldBeApproximately d1
            }
        }

        test("divOrNull returns null for non-invertible divisor") {
            checkAll(arbDual(), arbNonInvertibleDual()) { d1, d2 ->
                d1.divOrNull(d2) shouldBe null
            }
        }

        test("divOrNull returns quotient for invertible divisor") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val quotient = d1.divOrNull(d2)
                quotient shouldNotBe null

                val recovered = quotient!! * d2
                recovered shouldBeApproximately d1
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Operations (with field elements)
    // ------------------------------------------------------------------------

    context("Scalar operations") {

        test("dual plus scalar: (a+bε) + c = (a+c) + bε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { a, b, c ->
                val d = dual(a, b)
                val result = d + c

                result.a shouldBeApproximately (a + c)
                result.b shouldBeApproximately b
            }
        }

        test("dual minus scalar: (a+bε) - c = (a-c) + bε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { a, b, c ->
                val d = dual(a, b)
                val result = d - c

                result.a shouldBeApproximately (a - c)
                result.b shouldBeApproximately b
            }
        }

        test("dual times scalar: (a+bε) * c = (ac) + (bc)ε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { a, b, c ->
                val d = dual(a, b)
                val result = d * c

                result.a shouldBeApproximately (a * c)
                result.b shouldBeApproximately (b * c)
            }
        }

        test("dual divided by scalar: (a+bε) / c = (a/c) + (b/c)ε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbNonZeroDualDouble()) { a, b, c ->
                val d = dual(a, b)
                val result = d / c

                result.a shouldBeApproximately (a / c)
                result.b shouldBeApproximately (b / c)
            }
        }

        test("divOrNull with zero scalar returns null") {
            checkAll(arbDual()) { d ->
                d.divOrNull(0.0) shouldBe null
            }
        }
    }

    // ------------------------------------------------------------------------
    // Extension Functions (scalar from left)
    // ------------------------------------------------------------------------

    context("Extension functions") {

        test("scalar plus dual: c + (a+bε) = (c+a) + bε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { c, a, b ->
                val d = dual(a, b)
                val result = c + d

                result.a shouldBeApproximately (c + a)
                result.b shouldBeApproximately b
            }
        }

        test("scalar plus dual is commutative with dual plus scalar") {
            checkAll(arbDualDouble(), arbDual()) { c, d ->
                val left = c + d
                val right = d + c
                left shouldBeApproximately right
            }
        }

        test("scalar minus dual: c - (a+bε) = (c-a) - bε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { c, a, b ->
                val d = dual(a, b)
                val result = c - d

                result.a shouldBeApproximately (c - a)
                result.b shouldBeApproximately -b
            }
        }

        test("scalar times dual: c * (a+bε) = (ca) + (cb)ε") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { c, a, b ->
                val d = dual(a, b)
                val result = c * d

                result.a shouldBeApproximately (c * a)
                result.b shouldBeApproximately (c * b)
            }
        }

        test("scalar times dual is commutative with dual times scalar") {
            checkAll(arbDualDouble(), arbDual()) { c, d ->
                val left = c * d
                val right = d * c
                left shouldBeApproximately right
            }
        }

        test("scalar divided by dual: c / (a+bε)") {
            checkAll(arbNonZeroDualDouble(), arbNonZeroDualDouble(), arbDualDouble()) { c, a, b ->
                val d = dual(a, b)
                val result = c / d

                // Should equal c * (a+bε)⁻¹
                val expected = c * d.reciprocal()
                result shouldBeApproximately expected
            }
        }

        test("scalar divOrNull returns null for zero dual") {
            checkAll(arbDualDouble()) { c ->
                val zero = dualRing.add.identity
                c.divOrNull(zero) shouldBe null
            }
        }
    }

    // ------------------------------------------------------------------------
    // Complex Expressions
    // ------------------------------------------------------------------------

    context("Complex expressions") {

        test("polynomial evaluation: (a+bε)² = a² + 2abε") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val d = dual(a, b)
                val d2 = d * d

                val expectedA = a * a
                val expectedB = 2.0 * a * b

                d2.a shouldBeApproximately expectedA
                d2.b shouldBeApproximately expectedB
            }
        }

        test("polynomial: (a+bε)³ = a³ + 3a²bε") {
            checkAll(arbDualDouble(), arbDualDouble()) { a, b ->
                val d = dual(a, b)
                val d3 = d * d * d

                val expectedA = a * a * a
                val expectedB = 3.0 * a * a * b

                d3.a shouldBeApproximately expectedA
                d3.b shouldBeApproximately expectedB
            }
        }

        test("sum of products: d1*d2 + d3*d4") {
            checkAll(arbDual(), arbDual(), arbDual(), arbDual()) { d1, d2, d3, d4 ->
                val sum = d1 * d2 + d3 * d4

                // Verify through manual calculation
                val expectedA = d1.a * d2.a + d3.a * d4.a
                val expectedB = d1.a * d2.b + d1.b * d2.a + d3.a * d4.b + d3.b * d4.a

                sum.a shouldBeApproximately expectedA
                sum.b shouldBeApproximately expectedB
            }
        }

        test("fraction: (d1 + d2) / (d3 + d4)") {
            checkAll(arbDual(), arbDual(), arbInvertibleDual(), arbInvertibleDual()) { d1, d2, d3, d4 ->
                val numerator = d1 + d2
                val denominator = d3 + d4

                if (denominator.isInvertible()) {
                    val quotient = numerator / denominator
                    val recovered = quotient * denominator
                    recovered shouldBeApproximately numerator
                }
            }
        }

        test("mixed operations: (d1 + 5.0) * (d2 - 3.0)") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val result = (d1 + 5.0) * (d2 - 3.0)

                val left = dual(d1.a + 5.0, d1.b)
                val right = dual(d2.a - 3.0, d2.b)
                val expected = left * right

                result shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Automatic Differentiation Properties
    // ------------------------------------------------------------------------

    context("Automatic differentiation") {

        test("linear function derivative: f(x) = mx + b, f'(x) = m") {
            checkAll(arbDualDouble(), arbDualDouble(), arbDualDouble()) { x, m, b ->
                // Evaluate f(x + ε) = m(x + ε) + b = (mx + b) + mε
                val input = dual(x, 1.0)  // x + ε
                val result = m * input + b

                // Real part: f(x)
                result.a shouldBeApproximately m * x + b
                // Infinitesimal part: f'(x)
                result.b shouldBeApproximately m
            }
        }

        test("quadratic function derivative: f(x) = x², f'(x) = 2x") {
            checkAll(arbDualDouble()) { x ->
                val input = dual(x, 1.0)  // x + ε
                val result = input * input

                // Real part: x²
                result.a shouldBeApproximately (x * x)
                // Infinitesimal part: 2x
                result.b shouldBeApproximately (2.0 * x)
            }
        }

        test("cubic function derivative: f(x) = x³, f'(x) = 3x²") {
            checkAll(arbDualDouble()) { x ->
                val input = dual(x, 1.0)  // x + ε
                val result = input * input * input

                // Real part: x³
                result.a shouldBeApproximately (x * x * x)
                // Infinitesimal part: 3x²
                result.b shouldBeApproximately (3.0 * x * x)
            }
        }

        test("product rule: (fg)' = f'g + fg'") {
            checkAll(
                arbDualDouble(), arbDualDouble(),
                arbDualDouble(), arbDualDouble()
            ) { _, f0, g0, fPrime ->
                // f(x + ε) = f(x) + f'(x)ε, g(x + ε) = g(x) + g'(x)ε
                // We'll use specific values for testing
                val f = dual(f0, fPrime)
                val g = dual(g0, 1.0)  // g'(x) = 1

                val product = f * g

                // Product rule: (fg)' = f'g + fg'
                val expectedDerivative = fPrime * g0 + f0 * 1.0
                product.b shouldBeApproximately expectedDerivative
            }
        }

        test("chain rule through composition") {
            checkAll(arbDualDouble()) { x ->
                // Let f(u) = u² and u(x) = 3x
                // Then (f ∘ u)(x) = (3x)² = 9x²
                // And (f ∘ u)'(x) = 2(3x) * 3 = 18x

                val input = dual(x, 1.0)  // x + ε
                val u = 3.0 * input       // 3x + 3ε
                val result = u * u        // (3x)² with derivative

                // Real part: 9x²
                result.a shouldBeApproximately (9.0 * x * x)
                // Infinitesimal part: 18x (chain rule)
                result.b shouldBeApproximately (18.0 * x)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Edge Cases
    // ------------------------------------------------------------------------

    context("Edge cases") {

        test("operations on zero") {
            val zero = dualRing.add.identity
            val one = dualRing.mul.identity

            (zero + zero) shouldBeApproximately zero
            (zero * zero) shouldBeApproximately zero
            (zero * one) shouldBeApproximately zero
            (one * zero) shouldBeApproximately zero
        }

        test("operations on one") {
            val one = dualRing.mul.identity

            (one * one) shouldBeApproximately one
            (one / one) shouldBeApproximately one
        }

        test("operations on epsilon") {
            val e = dualRing.e
            val zero = dualRing.add.identity

            (e + e).a.shouldBeZero()
            (e + e).b shouldBeApproximately 2.0
            (e * e) shouldBeApproximately zero
        }

        test("very small infinitesimal parts") {
            checkAll(arbDualDouble()) { a ->
                val d = dual(a, 1e-10)
                val d2 = d * d

                // Even with tiny ε part, algebra is preserved
                d2.a shouldBeApproximately (a * a)
                d2.b shouldBeApproximately (2.0 * a * 1e-10)
            }
        }
    }

    // ------------------------------------------------------------------------
    // String Representation
    // ------------------------------------------------------------------------

    context("String representation") {

        test("toString format") {
            val d = dual(3.0, 4.0)
            d.toString() shouldBe "3.0 + 4.0ε"
        }

        test("zero dual toString") {
            val zero = dualRing.add.identity
            zero.toString() shouldBe "0.0 + 0.0ε"
        }

        test("one dual toString") {
            val one = dualRing.mul.identity
            one.toString() shouldBe "1.0 + 0.0ε"
        }

        test("epsilon toString") {
            val e = dualRing.e
            e.toString() shouldBe "0.0 + 1.0ε"
        }
    }
})