package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testutils.shouldBeApproximately
import org.vorpal.kosmos.testutils.shouldBeZero

class ScalarFieldSpec : FunSpec({
    lawTests(lawGraph("scalar fields") {
        // ------------------------------------------------------------------------
        // Basic Operations
        // ------------------------------------------------------------------------
        branch("ScalarField evaluation") {
            local("constant field returns same value everywhere") {
                lawCheckAll(arbFieldReal(), arbVec2Real()) { value, point ->
                    val field = ScalarField.constant(vec2Space, value)
                    field(point) shouldBeApproximately value
                }
            }

            local("zero field returns additive identity everywhere") {
                lawCheckAll(arbVec2Real()) { point ->
                    val field = ScalarField.zero(vec2Space)
                    field(point).shouldBeZero()
                }
            }

            local("one field returns multiplicative identity everywhere") {
                lawCheckAll(arbVec2Real()) { point ->
                    val field = ScalarField.one(vec2Space)
                    field(point) shouldBeApproximately 1.0
                }
            }

            local("of() creates field that evaluates correctly") {
                lawCheckAll(arbVec2Real()) { point ->
                    val field = ScalarField.of(vec2Space) { v -> v.x * 2.0 + v.y * 3.0 }
                    val expected = point.x * 2.0 + point.y * 3.0
                    field(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Pointwise Addition
        // ------------------------------------------------------------------------

        branch("Pointwise addition (+)") {
            local("addition is commutative: f + g = g + f") {
                lawCheckAll(arbScalarField(), arbScalarField(), arbVec2Real()) { f, g, point ->
                    val fg = f + g
                    val gf = g + f
                    fg(point) shouldBeApproximately gf(point)
                }
            }

            local("addition is associative: (f + g) + h = f + (g + h)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
                ) { f, g, h, point ->
                    val left = (f + g) + h
                    val right = f + (g + h)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("zero is additive identity: f + 0 = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val zero = ScalarField.zero(f.space)
                    val result = f + zero
                    result(point) shouldBeApproximately f(point)
                }
            }

            local("additive inverse: f + (-f) = 0") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val negF = -f
                    val sum = f + negF
                    sum(point).shouldBeZero()
                }
            }

            local("pointwise addition matches field addition") {
                lawCheckAll(arbScalarField(), arbScalarField(), arbVec2Real()) { f, g, point ->
                    val sum = f + g
                    val expected = RealAlgebras.RealField.add(f(point), g(point))
                    sum(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Pointwise Multiplication
        // ------------------------------------------------------------------------

        branch("Pointwise multiplication (*)") {
            local("multiplication is commutative: f * g = g * f") {
                lawCheckAll(arbScalarField(), arbScalarField(), arbVec2Real()) { f, g, point ->
                    val fg = f * g
                    val gf = g * f
                    fg(point) shouldBeApproximately gf(point)
                }
            }

            local("multiplication is associative: (f * g) * h = f * (g * h)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
                ) { f, g, h, point ->
                    val left = (f * g) * h
                    val right = f * (g * h)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("one is multiplicative identity: f * 1 = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val one = ScalarField.one(vec2Space)
                    val result = f * one
                    result(point) shouldBeApproximately f(point)
                }
            }

            local("multiplication by zero gives zero: f * 0 = 0") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val zero = ScalarField.zero(vec2Space)
                    val result = f * zero
                    result(point).shouldBeZero()
                }
            }

            local("pointwise multiplication matches field multiplication") {
                lawCheckAll(arbScalarField(), arbScalarField(), arbVec2Real()) { f, g, point ->
                    val product = f * g
                    val expected = RealAlgebras.RealField.mul(f(point), g(point))
                    product(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Pointwise Division
        // ------------------------------------------------------------------------

        branch("Pointwise division (/)") {
            local("division by one is identity: f / 1 = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val one = ScalarField.one(f.space)
                    val result = f / one
                    result(point) shouldBeApproximately f(point)
                }
            }

            local("self-division gives one (when non-zero): f / f = 1") {
                lawCheckAll(arbNonZeroScalarField(), arbVec2Real()) { f, point ->
                    val result = f / f
                    result(point) shouldBeApproximately 1.0
                }
            }

            local("division then multiplication recovers original: (f / g) * g = f") {
                lawCheckAll(
                    arbScalarField(),
                    arbNonZeroScalarField(),
                    arbVec2Real()
                ) { f, g, point ->
                    val divided = f / g
                    val recovered = divided * g
                    recovered(point) shouldBeApproximately f(point)
                }
            }

            local("pointwise division matches field division") {
                lawCheckAll(
                    arbScalarField(),
                    arbNonZeroScalarField(),
                    arbVec2Real()
                ) { f, g, point ->
                    val quotient = f / g
                    val gInverse = RealAlgebras.RealField.reciprocal(g(point))
                    val expected = RealAlgebras.RealField.mul(f(point), gInverse)
                    quotient(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Scalar Multiplication
        // ------------------------------------------------------------------------

        branch("Scalar multiplication") {
            local("scalar multiplication from right: f * c") {
                lawCheckAll(arbScalarField(), arbFieldReal(), arbVec2Real()) { f, c, point ->
                    val result = f * c
                    val expected = f(point) * c
                    result(point) shouldBeApproximately expected
                }
            }

            local("scalar multiplication from left: c * f") {
                lawCheckAll(arbFieldReal(), arbScalarField(), arbVec2Real()) { c, f, point ->
                    val result = c * f
                    val expected = c * f(point)
                    result(point) shouldBeApproximately expected
                }
            }

            local("scalar multiplication is commutative: c * f = f * c") {
                lawCheckAll(arbFieldReal(), arbScalarField(), arbVec2Real()) { c, f, point ->
                    val left = c * f
                    val right = f * c
                    left(point) shouldBe right(point)
                }
            }

            local("scalar multiplication distributes over addition: c * (f + g) = c*f + c*g") {
                lawCheckAll(
                    arbFieldReal(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
                ) { c, f, g, point ->
                    val left = c * (f + g)
                    val right = (c * f) + (c * g)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("scalar multiplication associates: (c * d) * f = c * (d * f)") {
                lawCheckAll(
                    arbFieldReal(),
                    arbFieldReal(),
                    arbScalarField(),
                    arbVec2Real()
                ) { c, d, f, point ->
                    val cd = c * d
                    val left = cd * f
                    val right = c * (d * f)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("multiplicative identity scales trivially: 1 * f = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val result = 1.0 * f
                    result(point) shouldBe f(point)
                }
            }

            local("zero scalar gives zero field: 0 * f = 0") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val result = 0.0 * f
                    result(point).shouldBeZero()
                }
            }
        }

        // ------------------------------------------------------------------------
        // Unary Negation
        // ------------------------------------------------------------------------

        branch("Unary negation") {
            local("negation inverts sign: (-f)(p) = -(f(p))") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val negF = -f
                    negF(point) shouldBeApproximately -f(point)
                }
            }

            local("Real negation is identity: -(-f) = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val result = -(-f)
                    result(point) shouldBeApproximately f(point)
                }
            }

            local("negation distributes over addition: -(f + g) = (-f) + (-g)") {
                lawCheckAll(arbScalarField(), arbScalarField(), arbVec2Real()) { f, g, point ->
                    val left = -(f + g)
                    val right = (-f) + (-g)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("negation relates to scalar multiplication: -f = (-1) * f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val negated = -f
                    val scaled = (-1.0) * f
                    negated(point) shouldBeApproximately scaled(point)
                }
            }
        }

        // ------------------------------------------------------------------------
        // andThen (Transformation)
        // ------------------------------------------------------------------------

        branch("andThen transformation") {
            local("andThen applies function pointwise") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val realDoubler = f.andThen { 2.0 * it }
                    realDoubler(point) shouldBeApproximately (f(point) * 2.0)
                }
            }

            local("andThen with identity is identity") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val mapped = Identity<Real>().compose(f)
                    mapped(point) shouldBeApproximately f(point)
                }
            }

            local("andThen composition") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val g: (Real) -> Real = { it * 2.0 }
                    val h: (Real) -> Real = { it + 5.0 }

                    val left = f.andThen(g).andThen(h)
                    val right = f.andThen { h(g(it)) }

                    left(point) shouldBeApproximately right(point)
                }
            }

            local("andThen preserves field structure through linear transformations") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbFieldReal(),
                    arbVec2Real()
                ) { f, g, c, point ->
                    val linear: (Real) -> Real = { it * c }
                    val mapped = (f + g).andThen(linear)
                    val separate = f.andThen(linear) + g.andThen(linear)

                    mapped(point) shouldBeApproximately separate(point)
                }
            }
        }

        // ------------------------------------------------------------------------
        // Functional Composition
        // ------------------------------------------------------------------------

        branch("Functional composition (∘)") {
            local("compose applies outer function to field result") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val Real: (Real) -> Real = { it * 2.0 }
                    val composed = Real compose f
                    composed(point) shouldBeApproximately (f(point) * 2.0)
                }
            }

            local("identity composition is identity: id ∘ f = f") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val id: (Real) -> Real = { it }
                    val composed = id compose f
                    composed(point) shouldBeApproximately f(point)
                }
            }

            local("composition associates: (h ∘ g) ∘ f = h ∘ (g ∘ f)") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val g: (Real) -> Real = { it * 2.0 }
                    val h: (Real) -> Real = { it + 10.0 }

                    val left = { x: Real -> h(g(x)) } compose f
                    val right = (h compose (g compose f))

                    left(point) shouldBeApproximately right(point)
                }
            }

            local("compose creates correct composite function") {
                lawCheckAll(arbVec2Real()) { point ->
                    val f = ScalarField.of(vec2Space) { v -> v.x + v.y }
                    val g: (Real) -> Real = { it * it }
                    val composed = g compose f

                    val expected = (point.x + point.y) * (point.x + point.y)
                    composed(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Distributivity Laws
        // ------------------------------------------------------------------------

        branch("Distributivity laws") {
            local("multiplication distributes over addition: f * (g + h) = f*g + f*h") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
                ) { f, g, h, point ->
                    val left = f * (g + h)
                    val right = (f * g) + (f * h)
                    left(point) shouldBeApproximately right(point)
                }
            }

            local("left distributivity: (f + g) * h = f*h + g*h") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
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

        branch("Combined operations") {
            local("complex expression: (f + g) * (h - k) evaluates correctly") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
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

            local("rational expression: (f + g) / (h + 1)") {
                lawCheckAll(
                    arbScalarField(),
                    arbScalarField(),
                    arbScalarField(),
                    arbVec2Real()
                ) { f, g, h, point ->
                    val one = ScalarField.one(f.space)
                    val numerator = f + g
                    val denominator = h + one
                    val result = numerator / denominator

                    val inv = h(point) + 1.0
                    if (inv != 0.0) {
                        val expected = (f(point) + g(point)) / inv
                        result(point) shouldBeApproximately expected
                    }
                }
            }

            local("field with scalar and andThen: (c * f).andThen { it + d }") {
                lawCheckAll(
                    arbFieldReal(),
                    arbFieldReal(),
                    arbScalarField(),
                    arbVec2Real()
                ) { c, d, f, point ->
                    val result = (c * f).andThen { it + d }
                    val expected = c * f(point) + d
                    result(point) shouldBeApproximately expected
                }
            }
        }

        // ------------------------------------------------------------------------
        // Edge Cases
        // ------------------------------------------------------------------------

        branch("Edge cases") {
            local("operations on constant fields") {
                lawCheckAll(arbFieldReal(), arbFieldReal(), arbVec2Real()) { a, b, point ->
                    val fieldA = ScalarField.constant(vec2Space, a)
                    val fieldB = ScalarField.constant(vec2Space, b)

                    (fieldA + fieldB)(point) shouldBeApproximately  (a + b)
                    (fieldA * fieldB)(point) shouldBeApproximately  (a * b)
                }
            }

            local("scalar multiplication by zero") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val zero = 0.0 * f
                    zero(point).shouldBeZero()
                }
            }

            local("addition of field with its negation") {
                lawCheckAll(arbScalarField(), arbVec2Real()) { f, point ->
                    val sum = f + (-f)
                    sum(point).shouldBeZero()
                }
            }
        }
    })
})
