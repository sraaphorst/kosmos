package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.powInt
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.ln
import kotlin.math.ln1p
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * Elementary functions on dual numbers over [Real].
 *
 * In this file, `f` denotes the primal part `a`, and `df` denotes the
 * tangent / infinitesimal coefficient `b`.
 *
 * For a dual number
 * ```text
 * x = a + bε
 * ```
 * with
 * ```text
 * ε² = 0,
 * ```
 * every sufficiently differentiable unary function satisfies
 * ```text
 * f(x) = f(a + bε) = f(a) + b f'(a) ε.
 * ```
 *
 * This is the core mechanism behind first-order forward-mode automatic differentiation:
 * the primal value is carried in `a`, and the derivative seed / tangent is carried in `b`.
 */
object DualRealFns {
    /**
     * Lift a differentiable unary real function to dual numbers.
     *
     * If
     * ```text
     * x = a + bε,
     * ```
     * then
     * ```text
     * f(x) = f(a) + b f'(a) ε.
     * ```
     */
    private inline fun liftUnary(
        x: Dual<Real>,
        f: (Real) -> Real,
        df: (Real) -> Real
    ): Dual<Real> =
        dual(
            f = f(x.f),
            df = x.df * df(x.f)
        )

    /**
     * Sine.
     * ```text
     * sin(a + bε) = sin(a) + b cos(a) ε
     * ```
     */
    fun sin(x: Dual<Real>): Dual<Real> =
        liftUnary(
            x = x,
            f = ::sin,
            df = ::cos
        )

    /**
     * Cosine.
     * ```text
     * cos(a + bε) = cos(a) - b sin(a) ε
     * ```
     */
    fun cos(x: Dual<Real>): Dual<Real> =
        liftUnary(
            x = x,
            f = ::cos,
            df = { -sin(it) }
        )

    /**
     * Tangent.
     * ```text
     * tan(a + bε) = tan(a) + b sec²(a) ε
     * ```
     * The function is undefined when `cos(a) = 0`.
     */
    fun tan(x: Dual<Real>): Dual<Real> {
        val ca = cos(x.f)
        require(ca != 0.0) { "tan undefined when cos(a)=0, got a=${x.f}" }

        val sec2 = 1.0 / (ca * ca)
        return dual(
            f = tan(x.f),
            df = x.df * sec2
        )
    }

    /**
     * Secant.
     * ```text
     * sec(a + bε) = sec(a) + b sec(a) tan(a) ε
     * ```
     * The function is undefined when `cos(a) = 0`.
     */
    fun sec(x: Dual<Real>): Dual<Real> {
        val ca = cos(x.f)
        require(ca != 0.0) { "sec undefined when cos(a)=0, got a=${x.f}" }

        val sa = 1.0 / ca
        val d = sa * tan(x.f)

        return dual(
            f = sa,
            df = x.df * d
        )
    }

    /**
     * Cotangent.
     * ```text
     * cot(a + bε) = cot(a) - b csc²(a) ε
     * ```
     * The function is undefined when `sin(a) = 0`.
     */
    fun cot(x: Dual<Real>): Dual<Real> {
        val sa = sin(x.f)
        require(sa != 0.0) { "cot undefined when sin(a)=0, got a=${x.f}" }

        val cotA = cos(x.f) / sa
        val csc2 = 1.0 / (sa * sa)

        return dual(
            f = cotA,
            df = -x.df * csc2
        )
    }

    /**
     * Cosecant.
     *
     * ```text
     * csc(a + bε) = csc(a) - b csc(a) cot(a) ε
     * ```
     * The function is undefined when `sin(a) = 0`.
     */
    fun csc(x: Dual<Real>): Dual<Real> {
        val sa = sin(x.f)
        require(sa != 0.0) { "csc undefined when sin(a)=0, got a=${x.f}" }

        val cscA = 1.0 / sa
        val cotA = cos(x.f) / sa

        return dual(
            f = cscA,
            df = -x.df * cscA * cotA
        )
    }

    /**
     * Exponential.
     * ```text
     * exp(a + bε) = exp(a) + b exp(a) ε
     * ```
     */
    fun exp(x: Dual<Real>): Dual<Real> =
        liftUnary(x, ::exp, ::exp)

    /**
     * Exponential minus one.
     * ```text
     * expm1(a + bε) = expm1(a) + b exp(a) ε
     * ```
     */
    fun expm1(x: Dual<Real>): Dual<Real> =
        dual(
            f = expm1(x.f),
            df = x.df * exp(x.f)
        )

    /**
     * Natural logarithm.
     * ```text
     * log(a + bε) = log(a) + (b / a) ε
     * ```
     * Requires `a > 0`.
     */
    fun log(x: Dual<Real>): Dual<Real> {
        require(x.f > 0.0) { "log requires positive real part, got ${x.f}" }

        return dual(
            f = ln(x.f),
            df = x.df / x.f
        )
    }

    /**
     * Natural logarithm of `1 + x`.
     * ```text
     * log1p(a + bε) = log1p(a) + b / (1 + a) ε
     * ```
     * Requires `a > -1`.
     */
    fun log1p(x: Dual<Real>): Dual<Real> {
        require(x.f > -1.0) { "log1p requires real part > -1, got ${x.f}" }

        return dual(
            f = ln1p(x.f),
            df = x.df / (1.0 + x.f)
        )
    }

    /**
     * Square root.
     * ```text
     * sqrt(a + bε) = sqrt(a) + b / (2 sqrt(a)) ε
     * ```
     * Requires `a >= 0`.
     * At `a = 0`, this returns `0 + 0ε` only when `b = 0`.
     */
    fun sqrt(x: Dual<Real>): Dual<Real> {
        require(x.f >= 0.0) { "sqrt requires nonnegative real part, got ${x.f}" }

        if (x.f == 0.0) {
            require(x.df == 0.0) { "sqrt derivative undefined at a=0 unless b=0" }
            return dual(
                f = 0.0,
                df = 0.0
            )
        }

        val sa = sqrt(x.f)
        return dual(
            f = sa,
            df = x.df / (2.0 * sa)
        )
    }

    /**
     * Arcsine.
     *
     * ```text
     * asin(a + bε) = asin(a) + b / sqrt(1 - a²) ε
     * ```
     * Requires `a ∈ [-1, 1]`. The derivative is undefined at `a = ±1` unless `b = 0`.
     */
    fun asin(x: Dual<Real>): Dual<Real> {
        require(x.f in -1.0..1.0) { "asin requires real part in [-1,1], got ${x.f}" }

        val denom2 = 1.0 - x.f * x.f
        if (denom2 == 0.0) {
            require(x.df == 0.0) { "asin derivative undefined at a=±1 unless b=0" }
            return dual(
                f = asin(x.f),
                df = 0.0
            )
        }

        return dual(
            f = asin(x.f),
            df = x.df / sqrt(denom2)
        )
    }

    /**
     * Arccosine.
     * ```text
     * acos(a + bε) = acos(a) - b / sqrt(1 - a²) ε
     * ```
     * Requires `a ∈ [-1, 1]`. The derivative is undefined at `a = ±1` unless `b = 0`.
     */
    fun acos(x: Dual<Real>): Dual<Real> {
        require(x.f in -1.0..1.0) { "acos requires real part in [-1,1], got ${x.f}" }

        val denom2 = 1.0 - x.f * x.f
        if (denom2 == 0.0) {
            require(x.df == 0.0) { "acos derivative undefined at a=±1 unless b=0" }
            return dual(
                f = acos(x.f),
                df = 0.0
            )
        }

        return dual(
            f = acos(x.f),
            df = -x.df / sqrt(denom2)
        )
    }

    /**
     * Arctangent.
     * ```text
     * atan(a + bε) = atan(a) + b / (1 + a²) ε
     * ```
     */
    fun atan(x: Dual<Real>): Dual<Real> =
        liftUnary(
            x = x,
            f = ::atan,
            df = { a -> 1.0 / (1.0 + a * a) }
        )

    /**
     * Hyperbolic sine.
     * ```text
     * sinh(a + bε) = sinh(a) + b cosh(a) ε
     * ```
     */
    fun sinh(x: Dual<Real>): Dual<Real> =
        liftUnary(x, ::sinh, ::cosh)

    /**
     * Hyperbolic cosine.
     * ```text
     * cosh(a + bε) = cosh(a) + b sinh(a) ε
     * ```
     */
    fun cosh(x: Dual<Real>): Dual<Real> =
        liftUnary(x, ::cosh, ::sinh)

    /**
     * Hyperbolic tangent.
     * ```text
     * tanh(a + bε) = tanh(a) + b sech²(a) ε
     * ```
     */
    fun tanh(x: Dual<Real>): Dual<Real> {
        val ta = tanh(x.f)
        val sech2 = 1.0 - ta * ta

        return dual(
            f = ta,
            df = x.df * sech2
        )
    }

    /**
     * Hyperbolic secant.
     * ```text
     * sech(a + bε) = sech(a) - b sech(a) tanh(a) ε
     * ```
     */
    fun sech(x: Dual<Real>): Dual<Real> {
        val ca = cosh(x.f)
        val sechA = 1.0 / ca
        val d = -sechA * tanh(x.f)

        return dual(
            f = sechA,
            df = x.df * d
        )
    }

    /**
     * Hyperbolic cotangent.
     * ```text
     * coth(a + bε) = coth(a) - b csch²(a) ε
     * ```
     * The function is undefined when `sinh(a) = 0`.
     */
    fun coth(x: Dual<Real>): Dual<Real> {
        val sa = sinh(x.f)
        require(sa != 0.0) { "coth undefined when sinh(a)=0, got a=${x.f}" }

        val cothA = cosh(x.f) / sa
        val csch2 = 1.0 / (sa * sa)

        return dual(
            f = cothA,
            df = -x.df * csch2
        )
    }

    /**
     * Hyperbolic cosecant.
     * ```text
     * csch(a + bε) = csch(a) - b csch(a) coth(a) ε
     * ```
     * The function is undefined when `sinh(a) = 0`.
     */
    fun csch(x: Dual<Real>): Dual<Real> {
        val sa = sinh(x.f)
        require(sa != 0.0) { "csch undefined when sinh(a)=0, got a=${x.f}" }

        val cschA = 1.0 / sa
        val cothA = cosh(x.f) / sa

        return dual(
            f = cschA,
            df = -x.df * cschA * cothA
        )
    }

    /**
     * Reciprocal.
     * ```text
     * (a + bε)^(-1) = a^(-1) - b / a² ε
     * ```
     * Requires `a != 0`.
     */
    fun inv(x: Dual<Real>): Dual<Real> {
        require(x.f != 0.0) { "inv requires nonzero real part, got ${x.f}" }

        val invA = 1.0 / x.f
        val invA2 = invA * invA

        return dual(
            f = invA,
            df = -x.df * invA2
        )
    }

    /**
     * Logistic sigmoid.
     * ```text
     * σ(a + bε) = σ(a) + b σ(a)(1 - σ(a)) ε
     * ```
     */
    fun sigmoid(x: Dual<Real>): Dual<Real> {
        val s = sigmoidReal(x.f)
        val ds = s * (1.0 - s)

        return dual(
            f = s,
            df = x.df * ds
        )
    }

    private fun sigmoidReal(x: Real): Real =
        if (x >= 0.0) {
            val z = exp(-x)
            1.0 / (1.0 + z)
        } else {
            val z = exp(x)
            z / (1.0 + z)
        }

    /**
     * Integer power with nonnegative exponent.
     * ```text
     * (a + bε)^n = a^n + n b a^(n-1) ε
     * ```
     * Requires `n >= 0`.
     */
    fun pow(x: Dual<Real>, n: Int): Dual<Real> {
        require(n >= 0) { "pow requires n >= 0" }

        val aPow = x.f.powInt(n)
        val bPart =
            if (n == 0) 0.0
            else n.toDouble() * x.df * x.f.powInt(n - 1)

        return dual(
            f = aPow,
            df = bPart
        )
    }

    /**
     * Real power.
     * ```text
     * (a + bε)^r = a^r + b r a^(r-1) ε
     * ```
     * This uses the principal real branch and therefore requires `a > 0`.
     */
    fun pow(x: Dual<Real>, r: Real): Dual<Real> {
        require(x.f > 0.0) { "pow(x, r) requires positive real part for the principal real branch, got ${x.f}" }

        val aPow = x.f.pow(r)
        val deriv = r * x.f.pow(r - 1.0)

        return dual(
            f = aPow,
            df = x.df * deriv
        )
    }
}
