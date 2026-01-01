package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RSpace
import org.vorpal.kosmos.algebra.structures.instances.VectorFieldAlgebra
import org.vorpal.kosmos.linear.Vec2R
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero
import kotlin.math.sqrt

class VectorFieldPropertyTest : FunSpec({

    // ------------------------------------------------------------------------
    // Basic Operations
    // ------------------------------------------------------------------------

    context("VectorField evaluation") {

        test("constant field returns same vector everywhere") {
            checkAll(arbVec2R(), arbVec2R()) { value, point ->
                val field = VectorFields.constant(Vec2RSpace, value)
                field(point) shouldBeApproximately value
            }
        }

        test("zero field returns zero vector everywhere") {
            checkAll(arbVec2R()) { point ->
                val field = VectorFields.zero(Vec2RSpace)
                field(point).shouldBeZero()
            }
        }

        test("of() creates field that evaluates correctly") {
            checkAll(arbVec2R()) { point ->
                val field = VectorFields.of(Vec2RSpace) { v ->
                    Vec2R(v.x * 2.0, v.y * 3.0)
                }
                val expected = Vec2R(point.x * 2.0, point.y * 3.0)
                field(point) shouldBeApproximately expected
            }
        }

        test("identity vector field preserves vectors") {
            checkAll(arbVec2R()) { point ->
                val identity = VectorFields.of(Vec2RSpace) { it }
                identity(point) shouldBeApproximately point
            }
        }
    }

    // ------------------------------------------------------------------------
    // Vector Addition (Abelian Group Laws)
    // ------------------------------------------------------------------------

    context("Vector addition (+)") {

        test("addition is commutative: x + Y = Y + x") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val xy = x + y
                val yx = y + x
                xy(point) shouldBeApproximately yx(point)
            }
        }

        test("addition is associative: (X + Y) + Z = x + (Y + Z)") {
            checkAll(
                arbVectorField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { x, y, z, point ->
                val left = (x + y) + z
                val right = x + (y + z)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("zero is additive identity: x + 0 = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val zero = VectorFields.zero(Vec2RSpace)
                val result = x + zero
                result(point) shouldBeApproximately x(point)
            }
        }

        test("additive inverse: x + (-x) = 0") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val negX = -x
                val sum = x + negX
                sum(point).shouldBeZero()
            }
        }

        test("pointwise addition uses vector space group operation") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val sum = x + y
                val expected = Vec2RSpace.group(x(point), y(point))
                sum(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Multiplication (by Field elements)
    // ------------------------------------------------------------------------

    context("Scalar multiplication (Field element)") {

        test("scalar multiplication from right: x * c") {
            checkAll(arbVectorField(), arbFieldReal(), arbVec2R()) { x, c, point ->
                val result = x * c
                val expected = Vec2RSpace.leftAction(c, x(point))
                result(point) shouldBeApproximately expected
            }
        }

        test("scalar multiplication from left: c * x") {
            checkAll(arbFieldReal(), arbVectorField(), arbVec2R()) { c, x, point ->
                val result = c * x
                val expected = Vec2RSpace.leftAction(c, x(point))
                result(point) shouldBeApproximately expected
            }
        }

        test("scalar multiplication is commutative: c * x = x * c") {
            checkAll(arbFieldReal(), arbVectorField(), arbVec2R()) { c, x, point ->
                val left = c * x
                val right = x * c
                left(point) shouldBeApproximately right(point)
            }
        }

        test("multiplicative identity: 1 * x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val result = 1.0 * x
                result(point) shouldBeApproximately x(point)
            }
        }

        test("zero scalar gives zero field: 0 * x = 0") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val result = 0.0 * x
                result(point).shouldBeZero()
            }
        }

        test("scalar multiplication associates: (c * d) * x = c * (d * x)") {
            checkAll(
                arbFieldReal(),
                arbFieldReal(),
                arbVectorField(),
                arbVec2R()
            ) { c, d, x, point ->
                val cd = c * d
                val left = cd * x
                val right = c * (d * x)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("scalar multiplication distributes over vector addition: c * (X + Y) = c*X + c*Y") {
            checkAll(
                arbFieldReal(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { c, x, y, point ->
                val left = c * (x + y)
                val right = (c * x) + (c * y)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("field addition distributes over scalar multiplication: (c + d) * x = c*X + d*X") {
            checkAll(
                arbFieldReal(),
                arbFieldReal(),
                arbVectorField(),
                arbVec2R()
            ) { c, d, x, point ->
                val cd = c + d
                val left = cd * x
                val right = (c * x) + (d * x)
                left(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Scalar Field Multiplication (Module Action)
    // ------------------------------------------------------------------------

    context("Scalar field multiplication (Module)") {

        test("scalar field multiplication: (f * x)(p) = f(p) · x(p)") {
            checkAll(
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, x, point ->
                val result = f * x
                val expected = Vec2RSpace.leftAction(f(point), x(point))
                result(point) shouldBeApproximately expected
            }
        }

        test("constant scalar field multiplication: c * x = (const_c) * x") {
            checkAll(
                arbFieldReal(),
                arbVectorField(),
                arbVec2R()
            ) { c, x, point ->
                val constField = ScalarFields.constant(Vec2RSpace, c)
                val fieldMul = constField * x
                val scalarMul = c * x
                fieldMul(point) shouldBeApproximately scalarMul(point)
            }
        }

        test("module action via actOn: f actOn x = f * x") {
            checkAll(
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, x, point ->
                val viaActOn = with(VectorFieldAlgebra) { f actOn x }
                val viaTimes = f * x
                viaActOn(point) shouldBeApproximately viaTimes(point)
            }
        }

        test("zero scalar field gives zero vector field: 0 * x = 0") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val zeroField = ScalarFields.zero(Vec2RSpace)
                val result = zeroField * x
                result(point).shouldBeZero()
            }
        }

        test("one scalar field is identity: 1 * x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val oneField = ScalarFields.one(Vec2RSpace)
                val result = oneField * x
                result(point) shouldBeApproximately x(point)
            }
        }

        test("scalar field multiplication distributes over vector addition: f * (X + Y) = f*X + f*Y") {
            checkAll(
                arbScalarField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { f, x, y, point ->
                val left = f * (x + y)
                val right = (f * x) + (f * y)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("scalar field addition distributes: (f + g) * x = f*X + g*X") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, g, x, point ->
                val left = (f + g) * x
                val right = (f * x) + (g * x)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("scalar field multiplication associates: (f * g) * x = f * (g * x)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, g, x, point ->
                val fg = f * g
                val left = fg * x

                // Note: g * x is scalar field times vector field.
                // f * (g * x) means we need to lift, or we interpret differently.
                // Actually, (g * x) is a vector field, and f * (vectorfield) works.
                val right = f * (g * x)

                left(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Unary Negation
    // ------------------------------------------------------------------------

    context("Unary negation") {

        test("negation inverts vectors: (-x)(p) = -(x(p))") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val negX = -x
                val expected = Vec2RSpace.group.inverse(x(point))
                negX(point) shouldBeApproximately expected
            }
        }

        test("Real negation is identity: -(-x) = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val result = -(-x)
                result(point) shouldBeApproximately x(point)
            }
        }

        test("negation distributes over addition: -(X + Y) = (-x) + (-Y)") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val left = -(x + y)
                val right = (-x) + (-y)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("negation relates to scalar multiplication: -x = (-1) * x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val negated = -x
                val scaled = (-1.0) * x
                negated(point) shouldBeApproximately scaled(point)
            }
        }

        test("negation of scalar times vector: -(c * x) = (-c) * x = c * (-x)") {
            checkAll(arbFieldReal(), arbVectorField(), arbVec2R()) { c, x, point ->
                val left = -(c * x)
                val middle = (-c) * x
                val right = c * (-x)

                left(point) shouldBeApproximately middle(point)
                middle(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Map (Transformation)
    // ------------------------------------------------------------------------

    context("Map transformation") {

        test("map applies function pointwise") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val Reald = x.map { v -> Vec2R(v.x * 2.0, v.y * 2.0) }
                val result = Reald(point)
                val original = x(point)
                result shouldBeApproximately Vec2R(original.x * 2.0, original.y * 2.0)
            }
        }

        test("map with identity is identity: x.map { it } = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val mapped = x.map { it }
                mapped(point) shouldBeApproximately x(point)
            }
        }

        test("map composition: x.map(f).map(g) = x.map(g ∘ f)") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val f: (Vec2R) -> Vec2R = { v -> Vec2R(v.x * 2.0, v.y * 2.0) }
                val g: (Vec2R) -> Vec2R = { v -> Vec2R(v.x + 1.0, v.y + 1.0) }

                val left = x.map(f).map(g)
                val right = x.map { g(f(it)) }

                left(point) shouldBeApproximately right(point)
            }
        }

        test("map distributes over addition when transformation is linear") {
            checkAll(
                arbVectorField(),
                arbVectorField(),
                arbFieldReal(),
                arbVec2R()
            ) { x, y, c, point ->
                val linear: (Vec2R) -> Vec2R = { v -> Vec2R(v.x * c, v.y * c) }
                val mapped = (x + y).map(linear)
                val separate = x.map(linear) + y.map(linear)

                mapped(point) shouldBeApproximately separate(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Vector Field Composition (then)
    // ------------------------------------------------------------------------

    context("Vector field composition (then)") {

        test("then composes fields: (X then Y)(p) = Y(x(p))") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val composed = x then y
                val expected = y(x(point))
                composed(point) shouldBeApproximately expected
            }
        }

        test("identity is right identity: x then id = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val identity = VectorFields.of(Vec2RSpace) { it }
                val composed = x then identity
                composed(point) shouldBeApproximately x(point)
            }
        }

        test("identity is left identity: id then x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val identity = VectorFields.of(Vec2RSpace) { it }
                val composed = identity then x
                composed(point) shouldBeApproximately x(point)
            }
        }

        test("composition associates: (X then Y) then Z = x then (Y then Z)") {
            checkAll(
                arbVectorField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { x, y, z, point ->
                val left = (x then y) then z
                val right = x then (y then z)
                left(point) shouldBeApproximately right(point)
            }
        }

        test("zero then x = x(0)") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val zero = VectorFields.zero(Vec2RSpace)
                val composed = zero then x
                val expected = x(Vec2R(0.0, 0.0))
                composed(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Functional Composition (compose with function)
    // ------------------------------------------------------------------------

    context("Functional composition") {

        test("function compose with vector field: (f ∘ x)(p) = f(x(p))") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val f: (Vec2R) -> Vec2R = { v -> Vec2R(v.x * 2.0, v.y * 3.0) }
                val composed = f compose x
                val expected = f(x(point))
                composed(point) shouldBeApproximately expected
            }
        }

        test("identity compose is identity: id ∘ x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val id: (Vec2R) -> Vec2R = { it }
                val composed = id compose x
                composed(point) shouldBeApproximately x(point)
            }
        }

        test("composition associates: (h ∘ g) ∘ x = h ∘ (g ∘ x)") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val g: (Vec2R) -> Vec2R = { v -> Vec2R(v.x * 2.0, v.y * 2.0) }
                val h: (Vec2R) -> Vec2R = { v -> Vec2R(v.x + 1.0, v.y + 1.0) }

                val left = { v: Vec2R -> h(g(v)) } compose x
                val right = h compose (g compose x)

                left(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Vector Field Composition (compose with vector field)
    // ------------------------------------------------------------------------

    context("Vector field composition (compose)") {

        test("vector field compose: (X compose Y)(p) = x(Y(p))") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val composed = x compose y
                val expected = x(y(point))
                composed(point) shouldBeApproximately expected
            }
        }

        test("compose and then are related: x compose Y = Y then x") {
            checkAll(arbVectorField(), arbVectorField(), arbVec2R()) { x, y, point ->
                val viaCompose = x compose y
                val viaThen = y then x
                viaCompose(point) shouldBeApproximately viaThen(point)
            }
        }

        test("identity is left identity for compose: id compose x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val identity = VectorFields.of(Vec2RSpace) { it }
                val composed = identity compose x
                composed(point) shouldBeApproximately x(point)
            }
        }

        test("identity is right identity for compose: x compose id = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val identity = VectorFields.of(Vec2RSpace) { it }
                val composed = x compose identity
                composed(point) shouldBeApproximately x(point)
            }
        }

        test("composition associates: (X compose Y) compose Z = x compose (Y compose Z)") {
            checkAll(
                arbVectorField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { x, y, z, point ->
                val left = (x compose y) compose z
                val right = x compose (y compose z)
                left(point) shouldBeApproximately right(point)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Module Laws (Comprehensive)
    // ------------------------------------------------------------------------

    context("Module laws") {

        test("module law: (f + g) actOn x = (f actOn x) + (g actOn x)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, g, x, point ->
                with(VectorFieldAlgebra) {
                    val left = (f + g) actOn x
                    val right = (f actOn x) + (g actOn x)
                    left(point) shouldBeApproximately right(point)
                }
            }
        }

        test("module law: f actOn (X + Y) = (f actOn x) + (f actOn Y)") {
            checkAll(
                arbScalarField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { f, x, y, point ->
                with(VectorFieldAlgebra) {
                    val left = f actOn (x + y)
                    val right = (f actOn x) + (f actOn y)
                    left(point) shouldBeApproximately right(point)
                }
            }
        }

        test("module law: (f * g) actOn x = f actOn (g actOn x)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbVectorField(),
                arbVec2R()
            ) { f, g, x, point ->
                with(VectorFieldAlgebra) {
                    val fg = f * g
                    val left = fg actOn x
                    val right = f actOn (g actOn x)
                    left(point) shouldBeApproximately right(point)
                }
            }
        }

        test("module law: 1 actOn x = x") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                with(VectorFieldAlgebra) {
                    val one = ScalarFields.one(Vec2RSpace)
                    val result = one actOn x
                    result(point) shouldBeApproximately x(point)
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Complex Combined Operations
    // ------------------------------------------------------------------------

    context("Combined operations") {

        test("complex expression: (f * x) + (g * Y)") {
            checkAll(
                arbScalarField(),
                arbScalarField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { f, g, x, y, point ->
                val result = (f * x) + (g * y)

                val fX = Vec2RSpace.leftAction(f(point), x(point))
                val gY = Vec2RSpace.leftAction(g(point), y(point))
                val expected = Vec2RSpace.group(fX, gY)

                result(point) shouldBeApproximately expected
            }
        }

        test("complex expression: c * (X + Y) - d * Z") {
            checkAll(
                arbFieldReal(),
                arbFieldReal(),
                arbVectorField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { c, d, x, y, z, point ->
                val result = (c * (x + y)) + ((-d) * z)

                val xy = Vec2RSpace.group(x(point), y(point))
                val cxy = Vec2RSpace.leftAction(c, xy)
                val dz = Vec2RSpace.leftAction(d, z(point))
                val negdz = Vec2RSpace.group.inverse(dz)
                val expected = Vec2RSpace.group(cxy, negdz)

                result(point) shouldBeApproximately expected
            }
        }

        test("composition with scaling: (c * x) then Y") {
            checkAll(
                arbFieldReal(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { c, x, y, point ->
                val scaled = c * x
                val composed = scaled then y

                val intermediate = Vec2RSpace.leftAction(c, x(point))
                val expected = y(intermediate)

                composed(point) shouldBeApproximately expected
            }
        }

        test("scalar field multiplication with composition: f * (X then Y)") {
            checkAll(
                arbScalarField(),
                arbVectorField(),
                arbVectorField(),
                arbVec2R()
            ) { f, x, y, point ->
                val composed = x then y
                val result = f * composed

                val composedValue = y(x(point))
                val expected = Vec2RSpace.leftAction(f(point), composedValue)

                result(point) shouldBeApproximately expected
            }
        }
    }

    // ------------------------------------------------------------------------
    // Edge Cases
    // ------------------------------------------------------------------------

    context("Edge cases") {

        test("constant vector field operations") {
            checkAll(arbVec2R(), arbVec2R(), arbVec2R()) { v1, v2, point ->
                val field1 = VectorFields.constant(Vec2RSpace, v1)
                val field2 = VectorFields.constant(Vec2RSpace, v2)

                val sum = field1 + field2
                val expected = Vec2RSpace.group(v1, v2)
                sum(point) shouldBeApproximately expected
            }
        }

        test("zero field operations") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val zero = VectorFields.zero(Vec2RSpace)
                val zeroVec = Vec2R(0.0, 0.0)

                (x + zero)(point) shouldBeApproximately x(point)
                (zero + x)(point) shouldBeApproximately x(point)
                (0.0 * x)(point) shouldBeApproximately zeroVec
            }
        }

        test("field with its negation cancels") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val sum = x + (-x)
                sum(point).shouldBeZero()
            }
        }

        test("composition with zero field") {
            checkAll(arbVectorField(), arbVec2R()) { x, point ->
                val zero = VectorFields.zero(Vec2RSpace)
                val zeroVec = Vec2R(0.0, 0.0)

                // zero then x should give x(0)
                val result1 = zero then x
                result1(point) shouldBeApproximately x(zeroVec)

                // x then zero should give 0
                val result2 = x then zero
                result2(point).shouldBeZero()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Rotation Vector Field Properties
    // ------------------------------------------------------------------------

    context("Rotation vector fields") {

        context("Rotation vector fields") {

            test("rotation field produces perpendicular vectors (unscaled)") {
                checkAll(arbVec2R()) { point ->
                    // Pure rotation: F(x, y) = (-y, x)
                    val rotation = VectorFields.of(Vec2RSpace) { v ->
                        Vec2R(-v.y, v.x)
                    }

                    val rotated = rotation(point)

                    // Rotated vector should be perpendicular: dot product = 0
                    val dotProduct = point.x * rotated.x + point.y * rotated.y
                    dotProduct.shouldBeZero()

                    // Magnitude should be preserved
                    val originalMag = sqrt(point.x * point.x + point.y * point.y)
                    val rotatedMag = sqrt(rotated.x * rotated.x + rotated.y * rotated.y)
                    rotatedMag shouldBeApproximately originalMag
                }
            }

            test("scaled rotation preserves perpendicularity") {
                checkAll(arbRotationVectorField(), arbVec2R()) { r, point ->
                    val rotated = r(point)

                    // Even with scaling, rotation should maintain perpendicularity
                    // (unless point is zero or scale is zero)
                    val pointMag = sqrt(point.x * point.x + point.y * point.y)
                    val rotatedMag = sqrt(rotated.x * rotated.x + rotated.y * rotated.y)

                    if (pointMag > 1e-6 && rotatedMag > 1e-6) {
                        // Normalized vectors should be perpendicular
                        val px = point.x / pointMag
                        val py = point.y / pointMag
                        val rx = rotated.x / rotatedMag
                        val ry = rotated.y / rotatedMag

                        val dotProduct = px * rx + py * ry
                        dotProduct.shouldBeZero()
                    }
                }
            }

            test("composition of rotations") {
                checkAll(arbVec2R()) { point ->
                    // Two 90° rotations = 180° rotation
                    val rotate90 = VectorFields.of(Vec2RSpace) { v -> Vec2R(-v.y, v.x) }
                    val composed = rotate90 then rotate90

                    val result = composed(point)
                    val expected = Vec2R(-point.x, -point.y)

                    result shouldBeApproximately expected
                }
            }
        }
    }
})