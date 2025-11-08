package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RSpace
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero


class ScalarFieldPropertyTest : FunSpec({

    // ------------------------------------------------------------------------
    // Basic Operations
    // ------------------------------------------------------------------------

    context("ScalarField evaluation") {

        test("constant field returns same value everywhere") {
            checkAll(arbFieldDouble(), arbVec2R()) { value, point ->
                val field = ScalarFields.constant(Vec2RSpace, value)
                field(point) shouldBeApproximately value
            }
        }

        test("zero field returns additive identity everywhere") {
            checkAll(arbVec2R()) { point ->
                val field = ScalarFields.zero(Vec2RSpace)
                field(point).shouldBeZero()
            }
        }

        test("one field returns multiplicative identity everywhere") {
            checkAll(arbVec2R()) { point ->
                val field = ScalarFields.one(Vec2RSpace)
                field(point) shouldBeApproximately 1.0
            }
        }

        test("of() creates field that evaluates correctly") {
            checkAll(arbVec2R()) { point ->
                val field = ScalarFields.of(Vec2RSpace) { v -> v.x * 2.0 + v.y * 3.0 }
                val expected = point.x * 2.0 + point.y * 3.0
                field(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Addition
    // ------------------------------------------------------------------------

    context("Pointwise addition (+)") {

        test("addition is commutative: f + g = g + f") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2R()) { f, g, point ->
                val fg = f + g
                val gf = g + f
                fg(point) shouldBeApproximately gf(point)
            }
        }

        test("addition is associative: (f + g) + h = f + (g + h)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, point ->
                val left = (f + g) + h
                val right = f + (g + h)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("zero is additive identity: f + 0 = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val zero = ScalarFields.zero(Vec2RSpace)
                val result = f + zero
                result(point) shouldBeApproximately f(point)
            }
        }

        test("additive inverse: f + (-f) = 0") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val negF = -f
                val sum = f + negF
                sum(point).shouldBeZero()
            }
        }

        test("pointwise addition matches field addition") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2R()) { f, g, point ->
                val sum = f + g
                val expected = RealField.add(f(point), g(point))
                sum(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Multiplication
    // ------------------------------------------------------------------------

    context("Pointwise multiplication (*)") {

        test("multiplication is commutative: f * g = g * f") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2R()) { f, g, point ->
                val fg = f * g
                val gf = g * f
                fg(point) shouldBeApproximately gf(point)
            }
        }

        test("multiplication is associative: (f * g) * h = f * (g * h)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, point ->
                val left = (f * g) * h
                val right = f * (g * h)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("one is multiplicative identity: f * 1 = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val one = ScalarFields.one(Vec2RSpace)
                val result = f * one
                result(point) shouldBeApproximately f(point)
            }
        }

        test("multiplication by zero gives zero: f * 0 = 0") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val zero = ScalarFields.zero(Vec2RSpace)
                val result = f * zero
                result(point).shouldBeZero()
            }
        }

        test("pointwise multiplication matches field multiplication") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2R()) { f, g, point ->
                val product = f * g
                val expected = RealField.mul(f(point), g(point))
                product(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Division
    // ------------------------------------------------------------------------

    context("Pointwise division (/)") {

        test("division by one is identity: f / 1 = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val one = ScalarFields.one(Vec2RSpace)
                val result = f / one
                result(point) shouldBeApproximately f(point)
            }
        }

        test("self-division gives one (when non-zero): f / f = 1") {
            checkAll(arbNonZeroScalarField(), arbVec2R()) { f, point ->
                val result = f / f
                result(point) shouldBeApproximately 1.0
            }
        }

        test("division then multiplication recovers original: (f / g) * g = f") {
            checkAll(
                arbScalarField(),
                arbNonZeroScalarField(),
                arbVec2R()
            ) { f, g, point ->
                val divided = f / g
                val recovered = divided * g
                recovered(point) shouldBeApproximately f(point)
            }
        }

        test("pointwise division matches field division") {
            checkAll(
                arbScalarField(),
                arbNonZeroScalarField(),
                arbVec2R()
            ) { f, g, point ->
                val quotient = f / g
                val gInverse = RealField.mul.inverse(g(point))
                val expected = RealField.mul(f(point), gInverse)
                quotient(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Multiplication
    // ------------------------------------------------------------------------

    context("Scalar multiplication") {

        test("scalar multiplication from right: f * c") {
            checkAll(arbScalarField(), arbFieldDouble(), arbVec2R()) { f, c, point ->
                val result = f * c
                val expected = f(point) * c
                result(point) shouldBeApproximately expected
            }
        }

        test("scalar multiplication from left: c * f") {
            checkAll(arbFieldDouble(), arbScalarField(), arbVec2R()) { c, f, point ->
                val result = c * f
                val expected = c * f(point)
                result(point) shouldBeApproximately expected
            }
        }

        test("scalar multiplication is commutative: c * f = f * c") {
            checkAll(arbFieldDouble(), arbScalarField(), arbVec2R()) { c, f, point ->
                val left = c * f
                val right = f * c
                left(point) shouldBe right(point)
            }
        }

        test("scalar multiplication distributes over addition: c * (f + g) = c*f + c*g") {
            checkAll(
                arbFieldDouble(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { c, f, g, point ->
                val left = c * (f + g)
                val right = (c * f) + (c * g)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("scalar multiplication associates: (c * d) * f = c * (d * f)") {
            checkAll(
                arbFieldDouble(),
                arbFieldDouble(),
                arbScalarField(),
                arbVec2R()
            ) { c, d, f, point ->
                val cd = c * d
                val left = cd * f
                val right = c * (d * f)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("multiplicative identity scales trivially: 1 * f = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val result = 1.0 * f
                result(point) shouldBe f(point)
            }
        }

        test("zero scalar gives zero field: 0 * f = 0") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val result = 0.0 * f
                result(point).shouldBeZero()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Unary Negation
    // ------------------------------------------------------------------------

    context("Unary negation") {

        test("negation inverts sign: (-f)(p) = -(f(p))") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val negF = -f
                negF(point) shouldBeApproximately -f(point)
            }
        }

        test("double negation is identity: -(-f) = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val result = -(-f)
                result(point) shouldBeApproximately f(point)
            }
        }

        test("negation distributes over addition: -(f + g) = (-f) + (-g)") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2R()) { f, g, point ->
                val left = -(f + g)
                val right = (-f) + (-g)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("negation relates to scalar multiplication: -f = (-1) * f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val negated = -f
                val scaled = (-1.0) * f
                negated(point) shouldBeApproximately scaled(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Map (Transformation)
    // ------------------------------------------------------------------------

    context("Map transformation") {

        test("map applies function pointwise") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val doubled = f.map { it * 2.0 }
                doubled(point) shouldBeApproximately (f(point) * 2.0)
            }
        }

        test("map with identity is identity: f.map { it } = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val mapped = f.map { it }
                mapped(point) shouldBeApproximately f(point)
            }
        }

        test("map composition: f.map(g).map(h) = f.map(g ∘ h)") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val g: (Double) -> Double = { it * 2.0 }
                val h: (Double) -> Double = { it + 5.0 }

                val left = f.map(g).map(h)
                val right = f.map { h(g(it)) }

                left(point) shouldBeApproximately right(point)
            }
        }

        test("map preserves field structure through linear transformations") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbFieldDouble(),
                arbVec2R()
            ) { f, g, c, point ->
                val linear: (Double) -> Double = { it * c }
                val mapped = (f + g).map(linear)
                val separate = f.map(linear) + g.map(linear)

                mapped(point) shouldBeApproximately separate(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Functional Composition
    // ------------------------------------------------------------------------

    context("Functional composition (∘)") {

        test("compose applies outer function to field result") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val double: (Double) -> Double = { it * 2.0 }
                val composed = double compose f
                composed(point) shouldBeApproximately (f(point) * 2.0)
            }
        }

        test("identity composition is identity: id ∘ f = f") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val id: (Double) -> Double = { it }
                val composed = id compose f
                composed(point) shouldBeApproximately f(point)
            }
        }

        test("composition associates: (h ∘ g) ∘ f = h ∘ (g ∘ f)") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val g: (Double) -> Double = { it * 2.0 }
                val h: (Double) -> Double = { it + 10.0 }

                val left = { x: Double -> h(g(x)) } compose f
                val right = (h compose (g compose f))

                left(point) shouldBeApproximately right(point)
            }
        }

        test("compose creates correct composite function") {
            checkAll(arbVec2R()) { point ->
                val f = ScalarFields.of(Vec2RSpace) { v -> v.x + v.y }
                val g: (Double) -> Double = { it * it }
                val composed = g compose f

                val expected = (point.x + point.y) * (point.x + point.y)
                composed(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Distributivity Laws
    // ------------------------------------------------------------------------

    context("Distributivity laws") {

        test("multiplication distributes over addition: f * (g + h) = f*g + f*h") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, point ->
                val left = f * (g + h)
                val right = (f * g) + (f * h)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("left distributivity: (f + g) * h = f*h + g*h") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, point ->
                val left = (f + g) * h
                val right = (f * h) + (g * h)
                left(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Complex Combined Operations
    // ------------------------------------------------------------------------

    context("Combined operations") {

        test("complex expression: (f + g) * (h - k) evaluates correctly") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, k, point ->
                val result = (f + g) * (h + (-k))

                val fVal = f(point)
                val gVal = g(point)
                val hVal = h(point)
                val kVal = k(point)
                val expected = (fVal + gVal) * (hVal - kVal)

                result(point) shouldBeApproximately expected
            }
        }

        test("rational expression: (f + g) / (h + 1)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2R()
            ) { f, g, h, point ->
                val one = ScalarFields.one(Vec2RSpace)
                val numerator = f + g
                val denominator = h + one
                val result = numerator / denominator

                val expected = (f(point) + g(point)) / (h(point) + 1.0)
                result(point) shouldBeApproximately expected
            }
        }

        test("field with scalar and map: (c * f).map { it + d }") {
            checkAll(
                arbFieldDouble(),
                arbFieldDouble(),
                arbScalarField(),
                arbVec2R()
            ) { c, d, f, point ->
                val result = (c * f).map { it + d }
                val expected = c * f(point) + d
                result(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Edge Cases
    // ------------------------------------------------------------------------

    context("Edge cases") {

        test("operations on constant fields") {
            checkAll(arbFieldDouble(), arbFieldDouble(), arbVec2R()) { a, b, point ->
                val fieldA = ScalarFields.constant(Vec2RSpace, a)
                val fieldB = ScalarFields.constant(Vec2RSpace, b)

                (fieldA + fieldB)(point) shouldBeApproximately  (a + b)
                (fieldA * fieldB)(point) shouldBeApproximately  (a * b)
            }
        }

        test("scalar multiplication by zero") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val zero = 0.0 * f
                zero(point).shouldBeZero()
            }
        }

        test("addition of field with its negation") {
            checkAll(arbScalarField(), arbVec2R()) { f, point ->
                val sum = f + (-f)
                sum(point).shouldBeZero()
            }
        }
    }
})
