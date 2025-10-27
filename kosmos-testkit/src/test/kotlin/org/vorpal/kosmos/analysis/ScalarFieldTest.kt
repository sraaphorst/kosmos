package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import kotlin.math.abs

// ============================================================================
// TEST IMPLEMENTATIONS: Field and VectorSpace
// ============================================================================

/**
 * Double field implementation for testing.
 */
object DoubleField : Field<Double> {
    override val add: AbelianGroup<Double> = AbelianGroup.of(
        op = Double::plus,
        identity = 0.0,
        inverse = Double::unaryMinus
    )

    override val mul: AbelianGroup<Double> = AbelianGroup.of(
        op = Double::times,
        identity = 1.0,
        inverse = { 1.0 / it }
    )
}

/**
 * Simple 2D vector for testing.
 */
data class Vec2D(val x: Double, val y: Double) {
    companion object {
        val ZERO = Vec2D(0.0, 0.0)
    }
}

/**
 * 2D vector space over doubles.
 */
object Vec2DSpace : VectorSpace<Double, Vec2D> {
    override val ring: Field<Double> = DoubleField

    override val group: AbelianGroup<Vec2D> = AbelianGroup.of(
        op = { a, b -> Vec2D(a.x + b.x, a.y + b.y) },
        identity = Vec2D.ZERO,
        inverse = { Vec2D(-it.x, -it.y) }
    )

    override val action: Action<Double, Vec2D> = Action { scalar, vec ->
        Vec2D(scalar * vec.x, scalar * vec.y)
    }
}

// ============================================================================
// KOTEST ARBITRARIES / GENERATORS
// ============================================================================

/**
 * Arbitrary for finite, non-NaN doubles suitable for field operations.
 */
fun arbFieldDouble(): Arb<Double> =
    Arb.double(-1000.0, 1000.0)
        .filter { it.isFinite() && !it.isNaN() }

/**
 * Arbitrary for non-zero doubles (for division tests).
 */
fun arbNonZeroDouble(): Arb<Double> =
    arbFieldDouble().filter { abs(it) > 1e-6 }

/**
 * Arbitrary for Vec2D vectors.
 */
fun arbVec2D(): Arb<Vec2D> = arbitrary {
    Vec2D(
        x = arbFieldDouble().bind(),
        y = arbFieldDouble().bind()
    )
}

/**
 * Arbitrary for scalar fields over Vec2D.
 */
fun arbScalarField(): Arb<ScalarField<Double, Vec2DSpace>> = arbitrary {
    val a = arbFieldDouble().bind()
    val b = arbFieldDouble().bind()
    val c = arbFieldDouble().bind()

    // Create linear scalar field: f(x, y) = a*x + b*y + c
    ScalarFields.of(Vec2DSpace) { v -> a * v.x + b * v.y + c }
}

/**
 * Arbitrary for non-zero scalar fields (for division tests).
 */
fun arbNonZeroScalarField(): Arb<ScalarField<Double, Vec2D>> = arbitrary {
    val a = arbNonZeroDouble().bind()
    val offset = arbNonZeroDouble().bind()

    // Create field that's always non-zero: f(x, y) = a*(x² + y² + 1) + offset
    ScalarFields.of(Vec2DSpace) { v ->
        a * (v.x * v.x + v.y * v.y + 1.0) + offset
    }
}

/**
 * Arbitrary for unary functions on doubles.
 */
fun arbDoubleFunction(): Arb<(Double) -> Double> = arbitrary {
    val choices = listOf<(Double) -> Double>(
        { it * 2.0 },
        { it + 10.0 },
        { it * it },
        { abs(it) },
        { if (it > 0) it else -it }
    )
    choices.random()
}

// ============================================================================
// PROPERTY-BASED TESTS
// ============================================================================

class ScalarFieldPropertyTest : FunSpec({

    val tolerance = 1e-9

    // ------------------------------------------------------------------------
    // Basic Operations
    // ------------------------------------------------------------------------

    context("ScalarField evaluation") {

        test("constant field returns same value everywhere") {
            checkAll(arbFieldDouble(), arbVec2D()) { value, point ->
                val field = ScalarFields.constant(Vec2DSpace, value)
                field(point) shouldBe (value plusOrMinus tolerance)
            }
        }

        test("zero field returns additive identity everywhere") {
            checkAll(arbVec2D()) { point ->
                val field = ScalarFields.zero(Vec2DSpace)
                field(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }

        test("one field returns multiplicative identity everywhere") {
            checkAll(arbVec2D()) { point ->
                val field = ScalarFields.one(Vec2DSpace)
                field(point) shouldBe (1.0 plusOrMinus tolerance)
            }
        }

        test("of() creates field that evaluates correctly") {
            checkAll(arbVec2D()) { point ->
                val field = ScalarFields.of(Vec2DSpace) { v -> v.x * 2.0 + v.y * 3.0 }
                val expected = point.x * 2.0 + point.y * 3.0
                field(point) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Addition
    // ------------------------------------------------------------------------

    context("Pointwise addition (+)") {

        test("addition is commutative: f + g = g + f") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2D()) { f, g, point ->
                val fg = f + g
                val gf = g + f
                fg(point) shouldBe (gf(point) plusOrMinus tolerance)
            }
        }

        test("addition is associative: (f + g) + h = f + (g + h)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2D()
            ) { f, g, h, point ->
                val left = (f + g) + h
                val right = f + (g + h)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("zero is additive identity: f + 0 = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val zero = ScalarFields.zero(Vec2DSpace)
                val result = f + zero
                result(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("additive inverse: f + (-f) = 0") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val negF = -f
                val sum = f + negF
                sum(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }

        test("pointwise addition matches field addition") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2D()) { f, g, point ->
                val sum = f + g
                val expected = DoubleField.add.op.combine(f(point), g(point))
                sum(point) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Multiplication
    // ------------------------------------------------------------------------

    context("Pointwise multiplication (*)") {

        test("multiplication is commutative: f * g = g * f") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2D()) { f, g, point ->
                val fg = f * g
                val gf = g * f
                fg(point) shouldBe (gf(point) plusOrMinus tolerance)
            }
        }

        test("multiplication is associative: (f * g) * h = f * (g * h)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2D()
            ) { f, g, h, point ->
                val left = (f * g) * h
                val right = f * (g * h)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("one is multiplicative identity: f * 1 = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val one = ScalarFields.one(Vec2DSpace)
                val result = f * one
                result(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("multiplication by zero gives zero: f * 0 = 0") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val zero = ScalarFields.zero(Vec2DSpace)
                val result = f * zero
                result(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }

        test("pointwise multiplication matches field multiplication") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2D()) { f, g, point ->
                val product = f * g
                val expected = DoubleField.mul.op.combine(f(point), g(point))
                product(point) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pointwise Division
    // ------------------------------------------------------------------------

    context("Pointwise division (/)") {

        test("division by one is identity: f / 1 = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val one = ScalarFields.one(Vec2DSpace)
                val result = f / one
                result(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("self-division gives one (when non-zero): f / f = 1") {
            checkAll(arbNonZeroScalarField(), arbVec2D()) { f, point ->
                val result = f / f
                result(point) shouldBe (1.0 plusOrMinus tolerance)
            }
        }

        test("division then multiplication recovers original: (f / g) * g = f") {
            checkAll(
                arbScalarField(),
                arbNonZeroScalarField(),
                arbVec2D()
            ) { f, g, point ->
                val divided = f / g
                val recovered = divided * g
                recovered(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("pointwise division matches field division") {
            checkAll(
                arbScalarField(),
                arbNonZeroScalarField(),
                arbVec2D()
            ) { f, g, point ->
                val quotient = f / g
                val gInverse = DoubleField.mul.inverse(g(point))
                val expected = DoubleField.mul.op.combine(f(point), gInverse)
                quotient(point) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Multiplication
    // ------------------------------------------------------------------------

    context("Scalar multiplication") {

        test("scalar multiplication from right: f * c") {
            checkAll(arbScalarField(), arbFieldDouble(), arbVec2D()) { f, c, point ->
                val result = f * c
                val expected = f(point) * c
                result(point) shouldBe (expected plusOrMinus tolerance)
            }
        }

        test("scalar multiplication from left: c * f") {
            checkAll(arbFieldDouble(), arbScalarField(), arbVec2D()) { c, f, point ->
                val result = c * f
                val expected = c * f(point)
                result(point) shouldBe (expected plusOrMinus tolerance)
            }
        }

        test("scalar multiplication is commutative: c * f = f * c") {
            checkAll(arbFieldDouble(), arbScalarField(), arbVec2D()) { c, f, point ->
                val left = c * f
                val right = f * c
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("scalar multiplication distributes over addition: c * (f + g) = c*f + c*g") {
            checkAll(
                arbFieldDouble(),
                arbScalarField(),
                arbScalarField(),
                arbVec2D()
            ) { c, f, g, point ->
                val left = c * (f + g)
                val right = (c * f) + (c * g)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("scalar multiplication associates: (c * d) * f = c * (d * f)") {
            checkAll(
                arbFieldDouble(),
                arbFieldDouble(),
                arbScalarField(),
                arbVec2D()
            ) { c, d, f, point ->
                val cd = c * d
                val left = cd * f
                val right = c * (d * f)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("multiplicative identity scales trivially: 1 * f = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val result = 1.0 * f
                result(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("zero scalar gives zero field: 0 * f = 0") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val result = 0.0 * f
                result(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Unary Negation
    // ------------------------------------------------------------------------

    context("Unary negation") {

        test("negation inverts sign: (-f)(p) = -(f(p))") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val negF = -f
                negF(point) shouldBe (-f(point) plusOrMinus tolerance)
            }
        }

        test("double negation is identity: -(-f) = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val result = -(-f)
                result(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("negation distributes over addition: -(f + g) = (-f) + (-g)") {
            checkAll(arbScalarField(), arbScalarField(), arbVec2D()) { f, g, point ->
                val left = -(f + g)
                val right = (-f) + (-g)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("negation relates to scalar multiplication: -f = (-1) * f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val negated = -f
                val scaled = (-1.0) * f
                negated(point) shouldBe (scaled(point) plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Map (Transformation)
    // ------------------------------------------------------------------------

    context("Map transformation") {

        test("map applies function pointwise") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val doubled = f.map { it * 2.0 }
                doubled(point) shouldBe (f(point) * 2.0 plusOrMinus tolerance)
            }
        }

        test("map with identity is identity: f.map { it } = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val mapped = f.map { it }
                mapped(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("map composition: f.map(g).map(h) = f.map(g ∘ h)") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val g: (Double) -> Double = { it * 2.0 }
                val h: (Double) -> Double = { it + 5.0 }

                val left = f.map(g).map(h)
                val right = f.map { h(g(it)) }

                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("map preserves field structure through linear transformations") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbFieldDouble(),
                arbVec2D()
            ) { f, g, c, point ->
                val linear: (Double) -> Double = { it * c }
                val mapped = (f + g).map(linear)
                val separate = f.map(linear) + g.map(linear)

                mapped(point) shouldBe (separate(point) plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Functional Composition
    // ------------------------------------------------------------------------

    context("Functional composition (∘)") {

        test("compose applies outer function to field result") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val double: (Double) -> Double = { it * 2.0 }
                val composed = double compose f
                composed(point) shouldBe (f(point) * 2.0 plusOrMinus tolerance)
            }
        }

        test("identity composition is identity: id ∘ f = f") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val id: (Double) -> Double = { it }
                val composed = id compose f
                composed(point) shouldBe (f(point) plusOrMinus tolerance)
            }
        }

        test("composition associates: (h ∘ g) ∘ f = h ∘ (g ∘ f)") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val g: (Double) -> Double = { it * 2.0 }
                val h: (Double) -> Double = { it + 10.0 }

                val left = { x: Double -> h(g(x)) } compose f
                val right = (h compose (g compose f))

                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("compose creates correct composite function") {
            checkAll(arbVec2D()) { point ->
                val f = ScalarFields.of(Vec2DSpace) { v -> v.x + v.y }
                val g: (Double) -> Double = { it * it }
                val composed = g compose f

                val expected = (point.x + point.y) * (point.x + point.y)
                composed(point) shouldBe (expected plusOrMinus tolerance)
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
                arbVec2D()
            ) { f, g, h, point ->
                val left = f * (g + h)
                val right = (f * g) + (f * h)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
            }
        }

        test("left distributivity: (f + g) * h = f*h + g*h") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2D()
            ) { f, g, h, point ->
                val left = (f + g) * h
                val right = (f * h) + (g * h)
                left(point) shouldBe (right(point) plusOrMinus tolerance)
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
                arbVec2D()
            ) { f, g, h, k, point ->
                val result = (f + g) * (h + (-k))

                val fVal = f(point)
                val gVal = g(point)
                val hVal = h(point)
                val kVal = k(point)
                val expected = (fVal + gVal) * (hVal - kVal)

                result(point) shouldBe (expected plusOrMinus tolerance)
            }
        }

        test("rational expression: (f + g) / (h + 1)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbScalarField(),
                arbVec2D()
            ) { f, g, h, point ->
                val one = ScalarFields.one(Vec2DSpace)
                val numerator = f + g
                val denominator = h + one
                val result = numerator / denominator

                val expected = (f(point) + g(point)) / (h(point) + 1.0)
                result(point) shouldBe (expected plusOrMinus tolerance)
            }
        }

        test("field with scalar and map: (c * f).map { it + d }") {
            checkAll(
                arbFieldDouble(),
                arbFieldDouble(),
                arbScalarField(),
                arbVec2D()
            ) { c, d, f, point ->
                val result = (c * f).map { it + d }
                val expected = c * f(point) + d
                result(point) shouldBe (expected plusOrMinus tolerance)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Edge Cases
    // ------------------------------------------------------------------------

    context("Edge cases") {

        test("operations on constant fields") {
            checkAll(arbFieldDouble(), arbFieldDouble(), arbVec2D()) { a, b, point ->
                val fieldA = ScalarFields.constant(Vec2DSpace, a)
                val fieldB = ScalarFields.constant(Vec2DSpace, b)

                (fieldA + fieldB)(point) shouldBe (a + b plusOrMinus tolerance)
                (fieldA * fieldB)(point) shouldBe (a * b plusOrMinus tolerance)
            }
        }

        test("scalar multiplication by zero") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val zero = 0.0 * f
                zero(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }

        test("addition of field with its negation") {
            checkAll(arbScalarField(), arbVec2D()) { f, point ->
                val sum = f + (-f)
                sum(point) shouldBe (0.0 plusOrMinus tolerance)
            }
        }
    }
})
