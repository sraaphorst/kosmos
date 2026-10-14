package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.core.math.Real
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

/**
 * The elementary functions on real dual numbers.
 *
 * Three independent kinds of evidence are used:
 * 1. **Closed forms.** Each function returns `f(a) + b·f'(a)·ε` for the textbook `f` and `f'`.
 * 2. **Finite differences.** Each stated derivative agrees with a central difference quotient of the
 *    function's own primal part, so a wrong derivative formula cannot hide behind a matching test
 *    formula.
 * 3. **Identities in the dual ring.** Relations such as `sin² + cos² = 1`, `tan = sin/cos` and
 *    `exp(x + y) = exp(x)·exp(y)` hold between *dual* numbers, tangent parts included, using the
 *    ring's own product and reciprocal.
 *
 * Failure modes are checked against the documented rule: an argument outside the domain is an
 * `IllegalArgumentException`; a pole is an `ArithmeticException`.
 */
class DualRealFnsSpec : StringSpec({
    val ring = DualAlgebras.RealDualRing
    val eq = DualAlgebras.eqDual
    val eqR = RealAlgebras.eqRealApprox
    val pr = DualAlgebras.printableDualPretty
    val arbTangent = Arb.double(-5.0..5.0)

    fun const(r: Real): Dual<Real> = dual(r, 0.0)
    fun plus(x: Dual<Real>, y: Dual<Real>) = ring.add(x, y)
    fun minus(x: Dual<Real>, y: Dual<Real>) = ring.add(x, ring.add.inverse(y))
    fun times(x: Dual<Real>, y: Dual<Real>) = ring.mul(x, y)
    fun over(x: Dual<Real>, y: Dual<Real>) = ring.mul(x, ring.reciprocal(y))

    fun shouldAgree(actual: Dual<Real>, expected: Dual<Real>, what: String) =
        withClue("$what: expected ${pr(expected)} but was ${pr(actual)}") { check(eq(actual, expected)) }

    /** A function under test: its dual lift, the real function, its derivative, and a safe domain. */
    data class Lifted(
        val name: String,
        val lift: (Dual<Real>) -> Dual<Real>,
        val f: (Real) -> Real,
        val df: (Real) -> Real,
        val domain: ClosedFloatingPointRange<Double>
    )

    fun sq(x: Real) = x * x
    fun sech(x: Real) = 1.0 / kotlin.math.cosh(x)
    fun logistic(x: Real) = 1.0 / (1.0 + kotlin.math.exp(-x))

    val functions = listOf(
        Lifted("sin", DualRealFns::sin, ::kSin, ::kCos, -6.0..6.0),
        Lifted("cos", DualRealFns::cos, ::kCos, { -kSin(it) }, -6.0..6.0),
        Lifted("tan", DualRealFns::tan, ::kTan, { 1.0 / sq(kCos(it)) }, -1.4..1.4),
        Lifted("sec", DualRealFns::sec, { 1.0 / kCos(it) }, { kTan(it) / kCos(it) }, -1.4..1.4),
        Lifted("cot", DualRealFns::cot, { kCos(it) / kSin(it) }, { -1.0 / sq(kSin(it)) }, 0.2..2.9),
        Lifted("csc", DualRealFns::csc, { 1.0 / kSin(it) }, { -kCos(it) / sq(kSin(it)) }, 0.2..2.9),
        Lifted("exp", DualRealFns::exp, ::kExp, ::kExp, -5.0..5.0),
        Lifted("expm1", DualRealFns::expm1, { kotlin.math.expm1(it) }, ::kExp, -5.0..5.0),
        Lifted("log", DualRealFns::log, { kotlin.math.ln(it) }, { 1.0 / it }, 0.1..50.0),
        Lifted("log1p", DualRealFns::log1p, { kotlin.math.ln1p(it) }, { 1.0 / (1.0 + it) }, -0.9..50.0),
        Lifted("sqrt", DualRealFns::sqrt, { kotlin.math.sqrt(it) }, { 0.5 / kotlin.math.sqrt(it) }, 0.1..50.0),
        Lifted("asin", DualRealFns::asin, { kotlin.math.asin(it) }, { 1.0 / kotlin.math.sqrt(1.0 - it * it) }, -0.95..0.95),
        Lifted("acos", DualRealFns::acos, { kotlin.math.acos(it) }, { -1.0 / kotlin.math.sqrt(1.0 - it * it) }, -0.95..0.95),
        Lifted("atan", DualRealFns::atan, { kotlin.math.atan(it) }, { 1.0 / (1.0 + it * it) }, -20.0..20.0),
        Lifted("sinh", DualRealFns::sinh, { kotlin.math.sinh(it) }, { kotlin.math.cosh(it) }, -4.0..4.0),
        Lifted("cosh", DualRealFns::cosh, { kotlin.math.cosh(it) }, { kotlin.math.sinh(it) }, -4.0..4.0),
        Lifted("tanh", DualRealFns::tanh, { kotlin.math.tanh(it) }, { sq(sech(it)) }, -4.0..4.0),
        Lifted("sech", DualRealFns::sech, ::sech, { -sech(it) * kotlin.math.tanh(it) }, -4.0..4.0),
        Lifted("coth", DualRealFns::coth, { 1.0 / kotlin.math.tanh(it) }, { -1.0 / sq(kotlin.math.sinh(it)) }, 0.2..4.0),
        Lifted("csch", DualRealFns::csch, { 1.0 / kotlin.math.sinh(it) }, { -kotlin.math.cosh(it) / sq(kotlin.math.sinh(it)) }, 0.2..4.0),
        Lifted("inv", DualRealFns::inv, { 1.0 / it }, { -1.0 / (it * it) }, 0.2..20.0),
        Lifted("sigmoid", DualRealFns::sigmoid, ::logistic, { logistic(it) * (1.0 - logistic(it)) }, -10.0..10.0)
    )

    // ── 1 and 2: closed forms and finite differences, for every function ─────

    functions.forEach { fn ->
        "${fn.name}: lifts to f(a) + b·f'(a)·ε" {
            checkAll(Arb.double(fn.domain), arbTangent) { a, b ->
                shouldAgree(fn.lift(dual(a, b)), dual(fn.f(a), b * fn.df(a)), "${fn.name}($a + ${b}ε)")
            }
        }

        "${fn.name}: its derivative agrees with a central difference quotient" {
            val h = 1e-6
            checkAll(Arb.double(fn.domain)) { a ->
                val derivative = fn.lift(dual(a, 1.0)).df
                val quotient = (fn.lift(const(a + h)).f - fn.lift(const(a - h)).f) / (2.0 * h)
                withClue("${fn.name}'($a): dual gives $derivative, difference quotient gives $quotient") {
                    check(abs(derivative - quotient) <= 1e-5 * (1.0 + abs(derivative)))
                }
            }
        }

        "${fn.name}: the tangent is linear in the seed, and a constant has tangent 0" {
            checkAll(Arb.double(fn.domain), arbTangent) { a, b ->
                check(eqR(fn.lift(dual(a, b)).df, b * fn.lift(dual(a, 1.0)).df))
                fn.lift(const(a)).df shouldBe (0.0 * fn.df(a))
            }
        }
    }

    // ── Integer and real powers ──────────────────────────────────────────────

    "pow(x, n) for n ≥ 0 is repeated multiplication in the ring, and aⁿ + n·aⁿ⁻¹·b·ε" {
        checkAll(Arb.double(-5.0..5.0), arbTangent, Arb.int(0..6)) { a, b, n ->
            val x = dual(a, b)
            shouldAgree(DualRealFns.pow(x, n), ring.mul.pow(x, n), "pow($x, $n)")
            if (n > 0) shouldAgree(DualRealFns.pow(x, n), dual(a.pow(n), n * a.pow(n - 1) * b), "pow($x, $n)")
        }
        DualRealFns.pow(dual(0.0, 3.0), 0) shouldBe dual(1.0, 0.0)
    }

    "pow(x, r) for real r lifts aʳ with derivative r·aʳ⁻¹" {
        checkAll(Arb.double(0.1..10.0), arbTangent, Arb.double(-3.0..3.0)) { a, b, r ->
            shouldAgree(DualRealFns.pow(dual(a, b), r), dual(a.pow(r), b * r * a.pow(r - 1.0)), "pow($a, $r)")
        }
    }

    "real powers specialize correctly: x¹ = x, x² = x·x, x^½ = sqrt(x), x⁻¹ = inv(x)" {
        checkAll(Arb.double(0.1..10.0), arbTangent) { a, b ->
            val x = dual(a, b)
            shouldAgree(DualRealFns.pow(x, 1.0), x, "x^1")
            shouldAgree(DualRealFns.pow(x, 2.0), times(x, x), "x^2")
            shouldAgree(DualRealFns.pow(x, 0.5), DualRealFns.sqrt(x), "x^(1/2)")
            shouldAgree(DualRealFns.pow(x, -1.0), DualRealFns.inv(x), "x^(-1)")
        }
    }

    "real powers add: xʳ·xˢ = xʳ⁺ˢ" {
        checkAll(Arb.double(0.2..5.0), arbTangent, Arb.double(-2.0..2.0), Arb.double(-2.0..2.0)) { a, b, r, s ->
            val x = dual(a, b)
            shouldAgree(times(DualRealFns.pow(x, r), DualRealFns.pow(x, s)), DualRealFns.pow(x, r + s), "x^r x^s")
        }
    }

    // ── 3: identities between dual numbers ───────────────────────────────────

    "Pythagorean identities: sin² + cos² = 1 and cosh² - sinh² = 1, tangents included" {
        checkAll(Arb.double(-6.0..6.0), arbTangent) { a, b ->
            val x = dual(a, b)
            val (s, c) = DualRealFns.sin(x) to DualRealFns.cos(x)
            shouldAgree(plus(times(s, s), times(c, c)), ring.one, "sin² + cos²")
        }
        checkAll(Arb.double(-3.0..3.0), arbTangent) { a, b ->
            val x = dual(a, b)
            val (sh, ch) = DualRealFns.sinh(x) to DualRealFns.cosh(x)
            shouldAgree(minus(times(ch, ch), times(sh, sh)), ring.one, "cosh² - sinh²")
        }
    }

    "the reciprocal and quotient functions are the ring's reciprocals and quotients" {
        checkAll(Arb.double(0.2..1.3), arbTangent) { a, b ->
            val x = dual(a, b)
            val (s, c) = DualRealFns.sin(x) to DualRealFns.cos(x)
            shouldAgree(DualRealFns.tan(x), over(s, c), "tan = sin/cos")
            shouldAgree(DualRealFns.sec(x), ring.reciprocal(c), "sec = 1/cos")
            shouldAgree(DualRealFns.cot(x), over(c, s), "cot = cos/sin")
            shouldAgree(DualRealFns.csc(x), ring.reciprocal(s), "csc = 1/sin")

            val (sh, ch) = DualRealFns.sinh(x) to DualRealFns.cosh(x)
            shouldAgree(DualRealFns.tanh(x), over(sh, ch), "tanh = sinh/cosh")
            shouldAgree(DualRealFns.sech(x), ring.reciprocal(ch), "sech = 1/cosh")
            shouldAgree(DualRealFns.coth(x), over(ch, sh), "coth = cosh/sinh")
            shouldAgree(DualRealFns.csch(x), ring.reciprocal(sh), "csch = 1/sinh")
        }
    }

    "inverse pairs compose to the identity" {
        checkAll(Arb.double(0.1..20.0), arbTangent) { a, b ->
            val x = dual(a, b)
            shouldAgree(DualRealFns.exp(DualRealFns.log(x)), x, "exp(log x)")
            shouldAgree(DualRealFns.log(DualRealFns.exp(dual(a - 10.0, b))), dual(a - 10.0, b), "log(exp x)")
            shouldAgree(times(DualRealFns.sqrt(x), DualRealFns.sqrt(x)), x, "sqrt(x)²")
            shouldAgree(DualRealFns.inv(DualRealFns.inv(x)), x, "inv(inv x)")
        }
        checkAll(Arb.double(-0.9..0.9), arbTangent) { a, b ->
            val x = dual(a, b)
            shouldAgree(DualRealFns.sin(DualRealFns.asin(x)), x, "sin(asin x)")
            shouldAgree(DualRealFns.cos(DualRealFns.acos(x)), x, "cos(acos x)")
            shouldAgree(DualRealFns.tan(DualRealFns.atan(x)), x, "tan(atan x)")
            // asin + acos is the constant π/2, so its tangent vanishes.
            shouldAgree(plus(DualRealFns.asin(x), DualRealFns.acos(x)), const(PI / 2), "asin + acos")
        }
    }

    "the shifted variants agree: expm1(x) = exp(x) - 1 and log1p(x) = log(1 + x)" {
        checkAll(Arb.double(-0.5..5.0), arbTangent) { a, b ->
            val x = dual(a, b)
            shouldAgree(DualRealFns.expm1(x), minus(DualRealFns.exp(x), ring.one), "expm1")
            shouldAgree(DualRealFns.log1p(x), DualRealFns.log(plus(ring.one, x)), "log1p")
        }
    }

    "functional equations: exp(x + y) = exp(x)·exp(y) and log(xy) = log(x) + log(y)" {
        checkAll(Arb.double(-3.0..3.0), arbTangent, Arb.double(-3.0..3.0), arbTangent) { a, b, c, d ->
            val (x, y) = dual(a, b) to dual(c, d)
            shouldAgree(DualRealFns.exp(plus(x, y)), times(DualRealFns.exp(x), DualRealFns.exp(y)), "exp(x + y)")
        }
        checkAll(Arb.double(0.1..10.0), arbTangent, Arb.double(0.1..10.0), arbTangent) { a, b, c, d ->
            val (x, y) = dual(a, b) to dual(c, d)
            shouldAgree(DualRealFns.log(times(x, y)), plus(DualRealFns.log(x), DualRealFns.log(y)), "log(xy)")
        }
    }

    "angle addition: sin(x + y) = sin(x)cos(y) + cos(x)sin(y)" {
        checkAll(Arb.double(-3.0..3.0), arbTangent, Arb.double(-3.0..3.0), arbTangent) { a, b, c, d ->
            val (x, y) = dual(a, b) to dual(c, d)
            shouldAgree(
                DualRealFns.sin(plus(x, y)),
                plus(times(DualRealFns.sin(x), DualRealFns.cos(y)), times(DualRealFns.cos(x), DualRealFns.sin(y))),
                "sin(x + y)"
            )
        }
    }

    "sigmoid(x) = 1/(1 + exp(-x)), and sigmoid(x) + sigmoid(-x) = 1" {
        checkAll(Arb.double(-10.0..10.0), arbTangent) { a, b ->
            val x = dual(a, b)
            val negX = ring.add.inverse(x)
            shouldAgree(DualRealFns.sigmoid(x), ring.reciprocal(plus(ring.one, DualRealFns.exp(negX))), "sigmoid")
            shouldAgree(plus(DualRealFns.sigmoid(x), DualRealFns.sigmoid(negX)), ring.one, "sigmoid symmetry")
        }
    }

    "sigmoid is stable far from the origin, where a naive formula would overflow" {
        DualRealFns.sigmoid(dual(800.0, 1.0)) shouldBe dual(1.0, 0.0)
        DualRealFns.sigmoid(dual(-800.0, 1.0)) shouldBe dual(0.0, 0.0)
    }

    // ── Boundary values that are allowed ─────────────────────────────────────

    "at a boundary with zero tangent the value is returned with tangent 0" {
        DualRealFns.sqrt(dual(0.0, 0.0)) shouldBe dual(0.0, 0.0)
        shouldAgree(DualRealFns.asin(dual(1.0, 0.0)), const(PI / 2), "asin(1)")
        shouldAgree(DualRealFns.asin(dual(-1.0, 0.0)), const(-PI / 2), "asin(-1)")
        shouldAgree(DualRealFns.acos(dual(1.0, 0.0)), const(0.0), "acos(1)")
        shouldAgree(DualRealFns.acos(dual(-1.0, 0.0)), const(PI), "acos(-1)")
    }

    // ── Failure modes ────────────────────────────────────────────────────────

    "arguments outside the domain are rejected with IllegalArgumentException" {
        listOf(0.0, -1.0, Double.NaN).forEach { a ->
            shouldThrow<IllegalArgumentException> { DualRealFns.log(dual(a, 1.0)) }
            shouldThrow<IllegalArgumentException> { DualRealFns.pow(dual(a, 1.0), 0.5) }
        }
        listOf(-1.0, -2.0, Double.NaN).forEach { a ->
            shouldThrow<IllegalArgumentException> { DualRealFns.log1p(dual(a, 1.0)) }
        }
        listOf(-1e-9, -4.0, Double.NaN).forEach { a ->
            shouldThrow<IllegalArgumentException> { DualRealFns.sqrt(dual(a, 1.0)) }
        }
        listOf(1.0000001, -3.0, Double.NaN).forEach { a ->
            shouldThrow<IllegalArgumentException> { DualRealFns.asin(dual(a, 0.0)) }
            shouldThrow<IllegalArgumentException> { DualRealFns.acos(dual(a, 0.0)) }
        }
        shouldThrow<IllegalArgumentException> { DualRealFns.pow(dual(2.0, 1.0), -1) }
    }

    "poles are rejected with ArithmeticException, including poles that are only approximately hit" {
        shouldThrow<ArithmeticException> { DualRealFns.cot(dual(0.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.csc(dual(0.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.coth(dual(0.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.csch(dual(0.0, 1.0)) }

        // cos(π/2) and sin(π) are about 1e-16 in floating point, not 0: an exact test would miss them.
        shouldThrow<ArithmeticException> { DualRealFns.tan(dual(PI / 2, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.sec(dual(PI / 2, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.tan(dual(-PI / 2, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.cot(dual(PI, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.csc(dual(PI, 1.0)) }
    }

    "an infinite derivative with a nonzero tangent is rejected with ArithmeticException" {
        shouldThrow<ArithmeticException> { DualRealFns.sqrt(dual(0.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.asin(dual(1.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.asin(dual(-1.0, -2.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.acos(dual(1.0, 1.0)) }
        shouldThrow<ArithmeticException> { DualRealFns.acos(dual(-1.0, 0.5)) }
    }

    "inv follows the floating-point reciprocal policy of the ring" {
        listOf(0.0, -0.0, 1e-300, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY).forEach { a ->
            ring.hasReciprocal(dual(a, 1.0)) shouldBe false
            shouldThrow<ArithmeticException> { DualRealFns.inv(dual(a, 1.0)) }
        }
        // A tangent that would overflow is rejected too.
        shouldThrow<ArithmeticException> { DualRealFns.inv(dual(1e-4, 1e301)) }

        checkAll(ArbDual.unitDual) { x ->
            DualRealFns.inv(x) shouldBe ring.reciprocal(x)
        }
    }
})

private fun kSin(x: Double): Double = kotlin.math.sin(x)
private fun kCos(x: Double): Double = kotlin.math.cos(x)
private fun kTan(x: Double): Double = kotlin.math.tan(x)
private fun kExp(x: Double): Double = kotlin.math.exp(x)
