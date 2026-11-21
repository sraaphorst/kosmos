package org.vorpal.kosmos.analysis

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.math.sqrt
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.analysis.instances.VectorFieldAlgebras
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instances.FixedVecAlgebras
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero

class VectorFieldSpec : FunSpec({
    lawTests(lawGraph("vector fields") {
        // ------------------------------------------------------------------------
        // Basic Operations
        // ------------------------------------------------------------------------

        branch("VectorField evaluation") {
            local("constant field returns same vector everywhere") {
                lawCheckAll(arbVec2Real(), arbVec2Real()) { value, point ->
                    val field = VectorField.constant(vec2Space, value)
                    field(point) shouldBeApproximately value
                }
            }

            local("zero field returns zero vector everywhere") {
                lawCheckAll(arbVec2Real()) { point ->
                    val field = VectorField.zero(vec2Space)
                    field(point).shouldBeZero()
                }
            }

            local("of() creates field that evaluates correctly") {
                lawCheckAll(arbVec2Real()) { point ->
                    val field = VectorField.of(vec2Space) { v ->
                        Vec2(v.x * 2.0, v.y * 3.0)
                    }
                    val expected = Vec2(point.x * 2.0, point.y * 3.0)
                    field(point) shouldBeApproximately expected
                }
            }

            local("identity vector field preserves vectors") {
                lawCheckAll(arbVec2Real()) { point ->
                    val identity = VectorField.of(vec2Space) { it }
                    identity(point) shouldBeApproximately point
                }
            }
        }

        // ------------------------------------------------------------------------
        // Vector Addition (Abelian Group Laws)
        // ------------------------------------------------------------------------

        branch("Vector addition (+)") {
            local("addition is commutative: x + Y = Y + x") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { x, y, point ->
                    val xy = x + y
                    val yx = y + x
                    xy(point) shouldBeApproximately yx(point)
                }
            }

            local("addition is associative: (X + Y) + Z = x + (Y + Z)") {
                lawCheckAll(
                    arbVectorField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { x, y, z, point ->
                    val left = (x + y) + z
                    val right = x + (y + z)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("zero is additive identity: x + 0 = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val zero = VectorField.zero(vec2Space)
                    val result = x + zero
                    result(point) shouldBeApproximately x(point)
                }
            }

            local("additive inverse: x + (-x) = 0") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val negX = -x
                    val sum = x + negX
                    sum(point).shouldBeZero()
                }
            }

            local("pointwise addition uses vector space group operation") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { x, y, point ->
                    val sum = x + y
                    val expected = vec2Space.add(x(point), y(point))
                    sum(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Scalar Multiplication (by Field elements)
        // ------------------------------------------------------------------------

        branch("Scalar multiplication (Field element)") {
            local("scalar multiplication from right: x * c") {
                lawCheckAll(arbVectorField(), arbFieldReal(), arbVec2Real()) { x, c, point ->
                    val result = x * c
                    val expected = vec2Space.leftAction(c, x(point))
                    result(point) shouldBeApproximately expected
                }
            }

            local("scalar multiplication from left: c * x") {
                lawCheckAll(arbFieldReal(), arbVectorField(), arbVec2Real()) { c, x, point ->
                    val result = c * x
                    val expected = vec2Space.leftAction(c, x(point))
                    result(point) shouldBeApproximately expected
                }
            }

            local("scalar multiplication is commutative: c * x = x * c") {
                lawCheckAll(arbFieldReal(), arbVectorField(), arbVec2Real()) { c, x, point ->
                    val left = c * x
                    val right = x * c
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("multiplicative identity: 1 * x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val result = 1.0 * x
                    result(point) shouldBeApproximately x(point)
                }
            }

            local("zero scalar gives zero field: 0 * x = 0") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val result = 0.0 * x
                    result(point).shouldBeZero()
                }
            }

            local("scalar multiplication associates: (c * d) * x = c * (d * x)") {
                lawCheckAll(
                    arbFieldReal(),
                    arbFieldReal(),
                    arbVectorField(),
                    arbVec2Real()
                ) { c, d, x, point ->
                    val cd = c * d
                    val left = cd * x
                    val right = c * (d * x)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("scalar multiplication distributes over vector addition: c * (X + Y) = c*X + c*Y") {
                lawCheckAll(
                    arbFieldReal(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { c, x, y, point ->
                    val left = c * (x + y)
                    val right = (c * x) + (c * y)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("field addition distributes over scalar multiplication: (c + d) * x = c*X + d*X") {
                lawCheckAll(
                    arbFieldReal(),
                    arbFieldReal(),
                    arbVectorField(),
                    arbVec2Real()
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

        branch("Scalar field multiplication (Module)") {
            local("scalar field multiplication: (f * x)(p) = f(p) · x(p)") {
                lawCheckAll(
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, x, point ->
                    val result = f * x
                    val expected = vec2Space.leftAction(f(point), x(point))
                    result(point) shouldBeApproximately expected
                }
            }

            local("constant scalar field multiplication: c * x = (const_c) * x") {
                lawCheckAll(
                    arbFieldReal(),
                    arbVectorField(),
                    arbVec2Real()
                ) { c, x, point ->
                    val constField = ScalarField.constant(vec2Space, c)
                    val fieldMul = constField * x
                    val scalarMul = c * x
                    fieldMul(point) shouldBeApproximately scalarMul(point)
                }
            }

            local("module action via actOn: f actOn x = f * x") {
                lawCheckAll(
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, x, point ->
                    val viaActOn = with(VectorFieldAlgebras.module(vec2Space)) { leftAction(f,x) }
                    val viaTimes = f * x
                    viaActOn(point) shouldBeApproximately viaTimes(point)
                }
            }

            local("zero scalar field gives zero vector field: 0 * x = 0") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val zeroField = ScalarField.zero(vec2Space)
                    val result = zeroField * x
                    result(point).shouldBeZero()
                }
            }

            local("one scalar field is identity: 1 * x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val oneField = ScalarField.one(vec2Space)
                    val result = oneField * x
                    result(point) shouldBeApproximately x(point)
                }
            }

            local("scalar field multiplication distributes over vector addition: f * (X + Y) = f*X + f*Y") {
                lawCheckAll(
                    arbScalarField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, x, y, point ->
                    val left = f * (x + y)
                    val right = (f * x) + (f * y)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("scalar field addition distributes: (f + g) * x = f*X + g*X") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, g, x, point ->
                    val left = (f + g) * x
                    val right = (f * x) + (g * x)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("scalar field multiplication associates: (f * g) * x = f * (g * x)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
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

        branch("Unary negation") {
            local("negation inverts vectors: (-x)(p) = -(x(p))") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val negX = -x
                    val expected = vec2Space.add.inverse(x(point))
                    negX(point) shouldBeApproximately expected
                }
            }

            local("Real negation is identity: -(-x) = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val result = -(-x)
                    result(point) shouldBeApproximately x(point)
                }
            }

            local("negation distributes over addition: -(X + Y) = (-x) + (-Y)") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { x, y, point ->
                    val left = -(x + y)
                    val right = (-x) + (-y)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("negation relates to scalar multiplication: -x = (-1) * x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val negated = -x
                    val scaled = (-1.0) * x
                    negated(point) shouldBeApproximately scaled(point)
                }
            }

            local("negation of scalar times vector: -(c * x) = (-c) * x = c * (-x)") {
                lawCheckAll(arbFieldReal(), arbVectorField(), arbVec2Real()) { c, x, point ->
                    val left = -(c * x)
                    val middle = (-c) * x
                    val right = c * (-x)

                    left(point) shouldBeApproximately middle(point)
                    middle(point) shouldBeApproximately right(point)
                }
            }
        }

        // ------------------------------------------------------------------------
        // andThen (Transformation)
        // ------------------------------------------------------------------------

        branch("andThen transformation") {
            local("map with identity is identity: x.map { it } = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, p ->
                    val id = VectorField.of(x.space) { it }
                    (x.andThen(id))(p) shouldBe x(p)
                    (id.andThen(x))(p) shouldBe x(p)
                }
            }

            local("andThen: associative") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVectorField(), arbVec2Real()) { f, g, h, p ->
                    // Require all share same space; easiest is: build g,h using f.space in the generators
                    // If your arbVectorField() already does, great. If not, see note below.
                    val left = (f.andThen(g).andThen(h))(p)
                    val right = (f.andThen(g.andThen(h)))(p)

                    left shouldBe right
                }
            }

            local("andThen: rejects different VectorSpace witnesses") {
                val s1 = FixedVecAlgebras.vec2Space(RealAlgebras.RealField)
                val s2 = FixedVecAlgebras.vec2Space(RealAlgebras.RealField) // different instance on purpose

                val f = VectorField.of(s1) { it }
                val g = VectorField.of(s2) { it }

                shouldThrow<IllegalArgumentException> {
                    f.andThen(g)
                }
            }

            local("andThen composition: (x.andThen(f)).andThen(g) = x.andThen(f.andThen(g))") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val fField = VectorField.of(x.space) { v -> Vec2(v.x * 2.0, v.y * 2.0) }
                    val gField = VectorField.of(x.space) { v -> Vec2(v.x + 1.0, v.y + 1.0) }

                    val left = x.andThen(fField).andThen(gField)
                    val right = x.andThen(fField.andThen(gField))

                    left(point) shouldBeApproximately right(point)
                }
            }

            local("andThen distributes over addition when the post-map is linear") {
                lawCheckAll(
                    arbVectorField(),
                    arbVectorField(),
                    arbFieldReal(),
                    arbVec2Real()
                ) { x, y, c, point ->
                    // Ensure x and y share the *same* space witness.
                    // If your generators don't guarantee that, see note below.

                    val lin = VectorField.of(x.space) { v ->
                        Vec2(v.x * c, v.y * c)
                    }

                    val mapped = (x + y).andThen(lin)
                    val separate = x.andThen(lin) + y.andThen(lin)

                    mapped(point) shouldBeApproximately separate(point)
                }
            }
        }

        // ------------------------------------------------------------------------
        // andThen (composition)
        // ------------------------------------------------------------------------

        branch("Vector field composition (andThen)") {
            local("then composes fields: (X then Y)(p) = Y(x(p))") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { x, y, point ->
                    val composed = x andThen y
                    val expected = y(x(point))
                    composed(point) shouldBeApproximately expected
                }
            }

            local("identity is right identity: x then id = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val identity = VectorField.of(vec2Space) { it }
                    val composed = x andThen identity
                    composed(point) shouldBeApproximately x(point)
                }
            }

            local("identity is left identity: id then x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val identity = VectorField.of(vec2Space) { it }
                    val composed = identity andThen x
                    composed(point) shouldBeApproximately x(point)
                }
            }

            local("composition associates: (X andThen Y) andThen Z = x andThen (Y andThen Z)") {
                lawCheckAll(
                    arbVectorField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { x, y, z, point ->
                    val left = (x andThen y) andThen z
                    val right = x andThen (y andThen z)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("zero andThen x = x(0)") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val zero = VectorField.zero(vec2Space)
                    val composed = zero andThen x
                    val expected = x(Vec2(0.0, 0.0))
                    composed(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // compose (composition)
        // ------------------------------------------------------------------------

        branch("Vector field composition (compose)") {
            local("vector field equates to composition: (f ∘ x)(p) = f(x(p))") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { f, x, point ->
                    val composed = f compose x
                    val expected = f(x(point))
                    composed(point) shouldBeApproximately expected
                }
            }

            local("identity composition is idempotent: id ∘ x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val identity = VectorField.of(vec2Space, Identity())
                    val composed = identity compose x
                    composed(point) shouldBeApproximately x(point)
                }
            }

            local("composition associates: (h ∘ g) ∘ x = h ∘ (g ∘ x)") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVectorField(), arbVec2Real()) { h, g, x, point ->
                    val left = h compose (g compose x)
                    val right = (h compose g) compose x
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("compose and andThen are related: x compose y = y andThen x") {
                lawCheckAll(arbVectorField(), arbVectorField(), arbVec2Real()) { x, y, point ->
                    val viaCompose = x compose y
                    val viaAndThen = y andThen x
                    viaCompose(point) shouldBeApproximately viaAndThen(point)
                }
            }

            local("identity is left identity for compose: id compose x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val identity = VectorField.of(vec2Space) { it }
                    val composed = identity compose x
                    composed(point) shouldBeApproximately x(point)
                }
            }

            local("identity is right identity for compose: x compose id = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val identity = VectorField.of(vec2Space) { it }
                    val composed = x compose identity
                    composed(point) shouldBeApproximately x(point)
                }
            }

            local("composition associates: (X compose Y) compose Z = x compose (Y compose Z)") {
                lawCheckAll(
                    arbVectorField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
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

        branch("Module laws") {
            val module = VectorFieldAlgebras.module(vec2Space)

            local("module law: (f + g) leftAction x = (f leftActon x) + (g leftAction x)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, g, x, point ->
                    with(module) {
                        val left = leftAction(scalars.add(f, g),  x)
                        val right = add(leftAction(f, x), leftAction(g, x))
                        left(point) shouldBeApproximately right(point)
                    }
                }
            }

            local("module law: f actOn (X + Y) = (f actOn x) + (f actOn Y)") {
                lawCheckAll(
                    arbScalarField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, x, y, point ->
                    with(module) {
                        val left = leftAction(f, add(x, y))
                        val right = add(leftAction(f, x), leftAction(f, y))
                        left(point) shouldBeApproximately right(point)
                    }
                }
            }

            local("module law: (f * g) actOn x = f actOn (g actOn x)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, g, x, point ->
                    with(module) {
                        val fg = f * g
                        val left = leftAction(fg, x)
                        val right = leftAction(f, leftAction(g, x))
                        left(point) shouldBeApproximately right(point)
                    }
                }
            }

            local("module law: 1 actOn x = x") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    with(module) {
                        val one = ScalarField.one(vec2Space)
                        val result = leftAction(one, x)
                        result(point) shouldBeApproximately x(point)
                    }
                }
            }
        }

        // ------------------------------------------------------------------------
        // Complex Combined Operations
        // ------------------------------------------------------------------------

        branch("Combined operations") {
            local("complex expression: (f * x) + (g * Y)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, g, x, y, point ->
                    val result = (f * x) + (g * y)

                    val fX = vec2Space.leftAction(f(point), x(point))
                    val gY = vec2Space.leftAction(g(point), y(point))
                    val expected = vec2Space.add(fX, gY)

                    result(point) shouldBeApproximately expected
                }
            }

            local("complex expression: c * (X + Y) - d * Z") {
                lawCheckAll(
                    arbFieldReal(),
                    arbFieldReal(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { c, d, x, y, z, point ->
                    val result = (c * (x + y)) + ((-d) * z)

                    val xy = vec2Space.add(x(point), y(point))
                    val cxy = vec2Space.leftAction(c, xy)
                    val dz = vec2Space.leftAction(d, z(point))
                    val negdz = vec2Space.add.inverse(dz)
                    val expected = vec2Space.add(cxy, negdz)

                    result(point) shouldBeApproximately expected
                }
            }

            local("composition with scaling: (c * x) then Y") {
                lawCheckAll(
                    arbFieldReal(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { c, x, y, point ->
                    val scaled = c * x
                    val composed = scaled andThen y

                    val intermediate = vec2Space.leftAction(c, x(point))
                    val expected = y(intermediate)

                    composed(point) shouldBeApproximately expected
                }
            }

            local("scalar field multiplication with composition: f * (X then Y)") {
                lawCheckAll(
                    arbScalarField(),
                    arbVectorField(),
                    arbVectorField(),
                    arbVec2Real()
                ) { f, x, y, point ->
                    val composed = x andThen y
                    val result = f * composed

                    val composedValue = y(x(point))
                    val expected = vec2Space.leftAction(f(point), composedValue)

                    result(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Edge Cases
        // ------------------------------------------------------------------------

        branch("Edge cases") {
            local("constant vector field operations") {
                lawCheckAll(arbVec2Real(), arbVec2Real(), arbVec2Real()) { v1, v2, point ->
                    val field1 = VectorField.constant(vec2Space, v1)
                    val field2 = VectorField.constant(vec2Space, v2)

                    val sum = field1 + field2
                    val expected = vec2Space.add(v1, v2)
                    sum(point) shouldBeApproximately expected
                }
            }

            local("zero field operations") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val zero = VectorField.zero(vec2Space)
                    val zeroVec = Vec2(0.0, 0.0)

                    (x + zero)(point) shouldBeApproximately x(point)
                    (zero + x)(point) shouldBeApproximately x(point)
                    (0.0 * x)(point) shouldBeApproximately zeroVec
                }
            }

            local("field with its negation cancels") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val sum = x + (-x)
                    sum(point).shouldBeZero()
                }
            }

            local("composition with zero field") {
                lawCheckAll(arbVectorField(), arbVec2Real()) { x, point ->
                    val zero = VectorField.zero(vec2Space)
                    val zeroVec = Vec2(0.0, 0.0)

                    // zero then x should give x(0)
                    val result1 = zero andThen x
                    result1(point) shouldBeApproximately x(zeroVec)

                    // x then zero should give 0
                    val result2 = x andThen zero
                    result2(point).shouldBeZero()
                }
            }
        }

        // ------------------------------------------------------------------------
        // Rotation Vector Field Properties
        // ------------------------------------------------------------------------

        branch("Rotation vector fields") {
            branch("Rotation vector fields") {
                local("rotation field produces perpendicular vectors (unscaled)") {
                    lawCheckAll(arbVec2Real()) { point ->
                        // Pure rotation: F(x, y) = (-y, x)
                        val rotation = VectorField.of(vec2Space) { v ->
                            Vec2(-v.y, v.x)
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

                local("scaled rotation preserves perpendicularity") {
                    lawCheckAll(arbRotationVectorField(), arbVec2Real()) { r, point ->
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

                local("composition of rotations") {
                    lawCheckAll(arbVec2Real()) { point ->
                        // Two 90° rotations = 180° rotation
                        val rotate90 = VectorField.of(vec2Space) { v -> Vec2(-v.y, v.x) }
                        val composed = rotate90 andThen rotate90

                        val result = composed(point)
                        val expected = Vec2(-point.x, -point.y)

                        result shouldBeApproximately expected
                    }
                }
            }
        }
    })
})
