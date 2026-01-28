package org.vorpal.kosmos.algebra.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.extensions.render.DualPrintables
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printables
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero

class DualRingPropertySpec : FunSpec({

    val dualRing = RealField.dual()
    val add = dualRing.add
    val mul = dualRing.mul
    val dualPr: Printable<Dual<Real>> = DualPrintables.dualSigned(Printables.real)
    val dualPrCompact: Printable<Dual<Real>> = DualPrintables.compactSigned(
        Printables.real, RealField, Eqs.realApprox()
    )

    fun dual(a: Real, b: Real): Dual<Real> =
        Dual(a, b)

    fun addD(x: Dual<Real>, y: Dual<Real>): Dual<Real> =
        add(x, y)

    fun negD(x: Dual<Real>): Dual<Real> =
        add.inverse(x)

    fun subD(x: Dual<Real>, y: Dual<Real>): Dual<Real> =
        addD(x, negD(y))

    fun mulD(x: Dual<Real>, y: Dual<Real>): Dual<Real> =
        mul(x, y)

    fun isInvertible(d: Dual<Real>): Boolean =
        d.a != 0.0

    fun reciprocalOrNull(d: Dual<Real>): Dual<Real>? =
        dualRing.reciprocalOrNull(d)

    fun reciprocal(d: Dual<Real>): Dual<Real> =
        dualRing.reciprocal(d)

    fun divOrNull(x: Dual<Real>, y: Dual<Real>): Dual<Real>? =
        reciprocalOrNull(y)?.let { invY -> mulD(x, invY) }

    fun div(x: Dual<Real>, y: Dual<Real>): Dual<Real> =
        mulD(x, reciprocal(y))

    // ------------------------------------------------------------------------
    // Basic Structure
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Addition (Abelian Group Laws)
    // ------------------------------------------------------------------------

    context("Addition - Abelian Group") {

        test("addition is commutative: d1 + d2 = d2 + d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = addD(d1, d2)
                val right = addD(d2, d1)
                left shouldBeApproximately right
            }
        }

        test("addition is associative: (d1 + d2) + d3 = d1 + (d2 + d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = addD(addD(d1, d2), d3)
                val right = addD(d1, addD(d2, d3))
                left shouldBeApproximately right
            }
        }

        test("additive identity: d + 0 = d") {
            checkAll(arbDual()) { d ->
                val zero = add.identity
                val result = addD(d, zero)
                result shouldBeApproximately d
            }
        }

        test("additive identity is 0 + 0ε") {
            val zero = add.identity
            zero.a shouldBe 0.0
            zero.b shouldBe 0.0
        }

        test("additive inverse: d + (-d) = 0") {
            checkAll(arbDual()) { d ->
                val neg = negD(d)
                val sum = addD(d, neg)
                val zero = add.identity
                sum shouldBeApproximately zero
            }
        }

        test("negation negates both components") {
            checkAll(arbDual()) { d ->
                val neg = negD(d)
                neg.a shouldBeApproximately -d.a
                neg.b shouldBeApproximately -d.b
            }
        }

        test("double negation: -(-d) = d") {
            checkAll(arbDual()) { d ->
                val neg = negD(d)
                val negNeg = negD(neg)
                negNeg shouldBeApproximately d
            }
        }

        test("addition components: (a₁+b₁ε) + (a₂+b₂ε) = (a₁+a₂) + (b₁+b₂)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal(), arbDualReal()) { a1, b1, a2, b2 ->
                val d1 = dual(a1, b1)
                val d2 = dual(a2, b2)
                val sum = addD(d1, d2)

                sum.a shouldBeApproximately (a1 + a2)
                sum.b shouldBeApproximately (b1 + b2)
            }
        }

        test("addition via op matches invoke") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val viaInvoke = add(d1, d2)
                val viaOp = add.op(d1, d2)
                viaInvoke shouldBeApproximately viaOp
            }
        }
    }

    // ------------------------------------------------------------------------
    // Multiplication (Monoid Laws)
    // ------------------------------------------------------------------------

    context("Multiplication - Monoid") {

        test("multiplication is associative: (d1 * d2) * d3 = d1 * (d2 * d3)") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = mulD(mulD(d1, d2), d3)
                val right = mulD(d1, mulD(d2, d3))
                left shouldBeApproximately right
            }
        }

        test("multiplication is commutative: d1 * d2 = d2 * d1") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = mulD(d1, d2)
                val right = mulD(d2, d1)
                left shouldBeApproximately right
            }
        }

        test("multiplicative identity: d * 1 = d") {
            checkAll(arbDual()) { d ->
                val one = mul.identity
                val result = mulD(d, one)
                result shouldBeApproximately d
            }
        }

        test("multiplicative identity is 1 + 0ε") {
            val one = mul.identity
            one.a shouldBe 1.0
            one.b shouldBe 0.0
        }

        test("multiplication formula: (a+bε)(c+dε) = ac + (ad+bc)ε") {
            checkAll(
                arbDualReal(), arbDualReal(),
                arbDualReal(), arbDualReal()
            ) { a, b, c, d ->
                val d1 = dual(a, b)
                val d2 = dual(c, d)
                val product = mulD(d1, d2)

                val expectedA = a * c
                val expectedB = a * d + b * c

                product.a shouldBeApproximately expectedA
                product.b shouldBeApproximately expectedB
            }
        }

        test("multiplication via op matches invoke") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val viaInvoke = mul(d1, d2)
                val viaOp = mul.op(d1, d2)
                viaInvoke shouldBeApproximately viaOp
            }
        }
    }

    // ------------------------------------------------------------------------
    // Distributivity (Ring Laws)
    // ------------------------------------------------------------------------

    context("Ring distributivity") {

        test("left distributivity: d1 * (d2 + d3) = d1*d2 + d1*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = mulD(d1, addD(d2, d3))
                val right = addD(mulD(d1, d2), mulD(d1, d3))
                left shouldBeApproximately right
            }
        }

        test("right distributivity: (d1 + d2) * d3 = d1*d3 + d2*d3") {
            checkAll(arbDual(), arbDual(), arbDual()) { d1, d2, d3 ->
                val left = mulD(addD(d1, d2), d3)
                val right = addD(mulD(d1, d3), mulD(d2, d3))
                left shouldBeApproximately right
            }
        }

        test("multiplication by zero gives zero: d * 0 = 0") {
            checkAll(arbDual()) { d ->
                val zero = add.identity
                val result = mulD(d, zero)
                result shouldBeApproximately zero
            }
        }
    }

    // ------------------------------------------------------------------------
    // Epsilon Properties (ε² = 0)
    // ------------------------------------------------------------------------

    context("Epsilon nilpotency") {

        test("epsilon squared is zero: ε² = 0") {
            val e = dualRing.epsOne
            val e2 = mulD(e, e)
            val zero = add.identity
            e2 shouldBeApproximately zero
        }

        test("pure infinitesimal squared is zero: (0+bε)² = 0") {
            checkAll(arbDualReal()) { b ->
                val d = dualRing.eps(b)
                val d2 = mulD(d, d)
                val zero = add.identity
                d2 shouldBeApproximately zero
            }
        }

        test("epsilon times epsilon is zero: ε * ε = 0") {
            val e = dualRing.epsOne
            val product = mulD(e, e)
            product.a.shouldBeZero()
            product.b.shouldBeZero()
        }

        test("any power of epsilon beyond 1 is zero") {
            checkAll(arbDualReal()) { b ->
                val eps = dualRing.eps(b)
                val eps2 = mulD(eps, eps)
                val eps3 = mulD(eps2, eps)

                val zero = add.identity
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
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val left = dualRing.lift(a + b)
                val right = addD(dualRing.lift(a), dualRing.lift(b))
                left shouldBeApproximately right
            }
        }

        test("lift preserves multiplication: lift(a * b) = lift(a) * lift(b)") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val left = dualRing.lift(a * b)
                val right = mulD(dualRing.lift(a), dualRing.lift(b))
                left shouldBeApproximately right
            }
        }

        test("lift preserves identity: lift(1) = 1 + 0ε") {
            val lifted = dualRing.lift(1.0)
            val one = mul.identity
            lifted shouldBeApproximately one
        }

        test("lift preserves zero: lift(0) = 0 + 0ε") {
            val lifted = dualRing.lift(0.0)
            val zero = add.identity
            lifted shouldBeApproximately zero
        }

        test("lifted elements commute with epsilon") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val lifted = dualRing.lift(a)
                val eps = dualRing.eps(b)

                val left = mulD(lifted, eps)
                val right = mulD(eps, lifted)
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
                val left = subD(d1, d2)
                val right = addD(d1, negD(d2))
                left shouldBeApproximately right
            }
        }

        test("self-subtraction gives zero: d - d = 0") {
            checkAll(arbDual()) { d ->
                val result = subD(d, d)
                val zero = add.identity
                result shouldBeApproximately zero
            }
        }

        test("subtraction components: (a₁+b₁ε) - (a₂+b₂ε) = (a₁-a₂) + (b₁-b₂)ε") {
            checkAll(
                arbDualReal(), arbDualReal(),
                arbDualReal(), arbDualReal()
            ) { a1, b1, a2, b2 ->
                val d1 = dual(a1, b1)
                val d2 = dual(a2, b2)
                val diff = subD(d1, d2)

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
                val expected = d.a != 0.0
                isInvertible(d) shouldBe expected
            }
        }

        test("lifted non-zero elements are invertible") {
            checkAll(arbNonZeroDualReal()) { a ->
                val d = dualRing.lift(a)
                isInvertible(d) shouldBe true
            }
        }

        test("pure infinitesimals are not invertible") {
            checkAll(arbNonZeroDualReal()) { b ->
                val d = dualRing.eps(b)
                isInvertible(d) shouldBe false
            }
        }

        test("zero is not invertible") {
            val zero = add.identity
            isInvertible(zero) shouldBe false
        }

        test("multiplicative identity is invertible") {
            val one = mul.identity
            isInvertible(one) shouldBe true
        }
    }

    // ------------------------------------------------------------------------
    // Reciprocal
    // ------------------------------------------------------------------------

    context("Reciprocal") {

        test("reciprocal of invertible dual: d * d⁻¹ = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = reciprocal(d)
                val product = mulD(d, inv)
                val one = mul.identity
                product shouldBeApproximately one
            }
        }

        test("reciprocal formula: (a+bε)⁻¹ = a⁻¹ - ba⁻²ε") {
            checkAll(arbNonZeroDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                val inv = reciprocal(d)

                val expectedA = 1.0 / a
                val expectedB = -b / (a * a)

                inv.a shouldBeApproximately expectedA
                inv.b shouldBeApproximately expectedB
            }
        }

        test("reciprocal is two-sided inverse: d⁻¹ * d = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = reciprocal(d)
                val product = mulD(inv, d)
                val one = mul.identity
                product shouldBeApproximately one
            }
        }

        test("reciprocal of lifted element: lift(a)⁻¹ = lift(a⁻¹)") {
            checkAll(arbNonZeroDualReal()) { a ->
                val d = dualRing.lift(a)
                val inv = reciprocal(d)
                val expected = dualRing.lift(1.0 / a)
                inv shouldBeApproximately expected
            }
        }

        test("reciprocalOrNull returns null for non-invertible") {
            checkAll(arbNonInvertibleDual()) { d ->
                reciprocalOrNull(d) shouldBe null
            }
        }

        test("reciprocalOrNull returns reciprocal for invertible") {
            checkAll(arbInvertibleDual()) { d ->
                val invOrNull = reciprocalOrNull(d)
                invOrNull shouldNotBe null

                val product = mulD(d, invOrNull!!)
                val one = mul.identity
                product shouldBeApproximately one
            }
        }

        test("double reciprocal: (d⁻¹)⁻¹ = d") {
            checkAll(arbInvertibleDual()) { d ->
                val inv = reciprocal(d)
                val invInv = reciprocal(inv)
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
                val one = mul.identity
                val result = div(d, one)
                result shouldBeApproximately d
            }
        }

        test("self-division gives one: d / d = 1") {
            checkAll(arbInvertibleDual()) { d ->
                val result = div(d, d)
                val one = mul.identity
                result shouldBeApproximately one
            }
        }

        test("division is multiplication by reciprocal: d1 / d2 = d1 * d2⁻¹") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val left = div(d1, d2)
                val right = mulD(d1, reciprocal(d2))
                left shouldBeApproximately right
            }
        }

        test("division then multiplication recovers original: (d1 / d2) * d2 = d1") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val divided = div(d1, d2)
                val recovered = mulD(divided, d2)
                recovered shouldBeApproximately d1
            }
        }

        test("divOrNull returns null for non-invertible divisor") {
            checkAll(arbDual(), arbNonInvertibleDual()) { d1, d2 ->
                divOrNull(d1, d2) shouldBe null
            }
        }

        test("divOrNull returns quotient for invertible divisor") {
            checkAll(arbDual(), arbInvertibleDual()) { d1, d2 ->
                val quotient = divOrNull(d1, d2)
                quotient shouldNotBe null

                val recovered = mulD(quotient!!, d2)
                recovered shouldBeApproximately d1
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Operations (with field elements)
    // ------------------------------------------------------------------------

    context("Scalar operations") {

        test("dual plus scalar: (a+bε) + c = (a+c) + bε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val d = dual(a, b)
                val result = addD(d, dualRing.lift(c))

                result.a shouldBeApproximately (a + c)
                result.b shouldBeApproximately b
            }
        }

        test("dual minus scalar: (a+bε) - c = (a-c) + bε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val d = dual(a, b)
                val result = subD(d, dualRing.lift(c))

                result.a shouldBeApproximately (a - c)
                result.b shouldBeApproximately b
            }
        }

        test("dual times scalar: (a+bε) * c = (ac) + (bc)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { a, b, c ->
                val d = dual(a, b)
                val result = mulD(d, dualRing.lift(c))

                result.a shouldBeApproximately (a * c)
                result.b shouldBeApproximately (b * c)
            }
        }

        test("dual divided by scalar: (a+bε) / c = (a/c) + (b/c)ε") {
            checkAll(arbDualReal(), arbDualReal(), arbNonZeroDualReal()) { a, b, c ->
                val d = dual(a, b)
                val result = div(d, dualRing.lift(c))

                result.a shouldBeApproximately (a / c)
                result.b shouldBeApproximately (b / c)
            }
        }

        test("divOrNull with zero scalar returns null") {
            checkAll(arbDual()) { d ->
                val q = divOrNull(d, dualRing.lift(0.0))
                q shouldBe null
            }
        }
    }

    // ------------------------------------------------------------------------
    // Complex Expressions
    // ------------------------------------------------------------------------

    context("Complex expressions") {

        test("polynomial evaluation: (a+bε)² = a² + 2abε") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                val d2 = mulD(d, d)

                val expectedA = a * a
                val expectedB = 2.0 * a * b

                d2.a shouldBeApproximately expectedA
                d2.b shouldBeApproximately expectedB
            }
        }

        test("polynomial: (a+bε)³ = a³ + 3a²bε") {
            checkAll(arbDualReal(), arbDualReal()) { a, b ->
                val d = dual(a, b)
                val d3 = mulD(mulD(d, d), d)

                val expectedA = a * a * a
                val expectedB = 3.0 * a * a * b

                d3.a shouldBeApproximately expectedA
                d3.b shouldBeApproximately expectedB
            }
        }

        test("sum of products: d1*d2 + d3*d4") {
            checkAll(arbDual(), arbDual(), arbDual(), arbDual()) { d1, d2, d3, d4 ->
                val sum = addD(mulD(d1, d2), mulD(d3, d4))

                val expectedA = d1.a * d2.a + d3.a * d4.a
                val expectedB = d1.a * d2.b + d1.b * d2.a + d3.a * d4.b + d3.b * d4.a

                sum.a shouldBeApproximately expectedA
                sum.b shouldBeApproximately expectedB
            }
        }

        test("fraction: (d1 + d2) / (d3 + d4)") {
            checkAll(arbDual(), arbDual(), arbInvertibleDual(), arbInvertibleDual()) { d1, d2, d3, d4 ->
                val numerator = addD(d1, d2)
                val denominator = addD(d3, d4)

                if (isInvertible(denominator)) {
                    val quotient = div(numerator, denominator)
                    val recovered = mulD(quotient, denominator)
                    recovered shouldBeApproximately numerator
                }
            }
        }

        test("mixed operations: (d1 + 5.0) * (d2 - 3.0)") {
            checkAll(arbDual(), arbDual()) { d1, d2 ->
                val left = addD(d1, dualRing.lift(5.0))
                val right = subD(d2, dualRing.lift(3.0))
                val result = mulD(left, right)

                val expectedLeft = dual(d1.a + 5.0, d1.b)
                val expectedRight = dual(d2.a - 3.0, d2.b)
                val expected = mulD(expectedLeft, expectedRight)

                result shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Automatic Differentiation Properties
    // ------------------------------------------------------------------------

    context("Automatic differentiation") {

        test("linear function derivative: f(x) = mx + b, f'(x) = m") {
            checkAll(arbDualReal(), arbDualReal(), arbDualReal()) { x, m, b ->
                val input = dual(x, 1.0)  // x + ε
                val result = addD(mulD(dualRing.lift(m), input), dualRing.lift(b))

                result.a shouldBeApproximately m * x + b
                result.b shouldBeApproximately m
            }
        }

        test("quadratic function derivative: f(x) = x², f'(x) = 2x") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0)
                val result = mulD(input, input)

                result.a shouldBeApproximately (x * x)
                result.b shouldBeApproximately (2.0 * x)
            }
        }

        test("cubic function derivative: f(x) = x³, f'(x) = 3x²") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0)
                val result = mulD(mulD(input, input), input)

                result.a shouldBeApproximately (x * x * x)
                result.b shouldBeApproximately (3.0 * x * x)
            }
        }

        test("product rule: (fg)' = f'g + fg'") {
            checkAll(
                arbDualReal(), arbDualReal(),
                arbDualReal(), arbDualReal()
            ) { _, f0, g0, fPrime ->
                val f = dual(f0, fPrime)
                val g = dual(g0, 1.0)  // g'(x) = 1

                val product = mulD(f, g)

                val expectedDerivative = fPrime * g0 + f0 * 1.0
                product.b shouldBeApproximately expectedDerivative
            }
        }

        test("chain rule through composition") {
            checkAll(arbDualReal()) { x ->
                val input = dual(x, 1.0) // x + ε
                val u = mulD(dualRing.lift(3.0), input) // 3x + 3ε
                val result = mulD(u, u)

                result.a shouldBeApproximately (9.0 * x * x)
                result.b shouldBeApproximately (18.0 * x)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Edge Cases
    // ------------------------------------------------------------------------

    context("Edge cases") {

        test("operations on zero") {
            val zero = add.identity
            val one = mul.identity

            addD(zero, zero) shouldBeApproximately zero
            mulD(zero, zero) shouldBeApproximately zero
            mulD(zero, one) shouldBeApproximately zero
            mulD(one, zero) shouldBeApproximately zero
        }

        test("operations on one") {
            val one = mul.identity

            mulD(one, one) shouldBeApproximately one
            div(one, one) shouldBeApproximately one
        }

        test("operations on epsilon") {
            val e = dualRing.epsOne
            val zero = add.identity

            val twoE = addD(e, e)
            twoE.a.shouldBeZero()
            twoE.b shouldBeApproximately 2.0

            mulD(e, e) shouldBeApproximately zero
        }

        test("very small infinitesimal parts") {
            checkAll(arbDualReal()) { a ->
                val d = dual(a, 1e-10)
                val d2 = mulD(d, d)

                d2.a shouldBeApproximately (a * a)
                d2.b shouldBeApproximately (2.0 * a * 1e-10)
            }
        }
    }

    // ------------------------------------------------------------------------
    // String Representation
    // ------------------------------------------------------------------------

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
            val zero = add.identity
            dualPr(zero) shouldBe "0.0 + 0.0$eps"
            dualPrCompact(zero) shouldBe "0.0"
        }

        test("one dual toString") {
            val one = mul.identity
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