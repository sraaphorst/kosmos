package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational

/**
 * Elementary algebraic functions on dual numbers over [Rational].
 *
 *  In this file, `f` denotes the primal part `a`, and `df` denotes the
 *  tangent / infinitesimal coefficient `b`.
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
 * Over the rationals, the most natural functions are algebraic ones such as inversion
 * and integer powers. Transcendental functions like `exp`, `sin`, and `log` generally
 * do not stay inside [Rational]: thus, they do not belong here.
 */
object DualRationalFns {
    /**
     * Reciprocal.
     * ```text
     * (a + bε)^(-1) = a^(-1) - b / a² ε
     * ```
     * Requires `a != 0`.
     */
    fun inv(x: Dual<Rational>): Dual<Rational> {
        require(x.f != Rational.ZERO) { "inv requires nonzero real part, got ${x.f}" }

        val invA = x.f.reciprocal()
        val invA2 = invA * invA

        return dual(
            f = invA,
            df = -x.df * invA2
        )
    }

    /**
     * Integer power with nonnegative exponent.
     * ```text
     * (a + bε)^n = a^n + n b a^(n-1) ε
     * ```
     * Requires `n >= 0`.
     */
    fun pow(x: Dual<Rational>, n: Int): Dual<Rational> {
        require(n != Int.MIN_VALUE) { "pow exponent Int.MIN_VALUE is not supported" }
        if (n == 0) return dual(f = Rational.ONE, df = Rational.ZERO)

        val aPow = x.f.pow(n)
        val bPart = n.toRational() * x.df * x.f.pow(n - 1)

        return dual(
            f = aPow,
            df = bPart
        )
    }

    /**
     * Square.
     * ```text
     * (a + bε)² = a² + 2ab ε
     * ```
     */
    fun square(x: Dual<Rational>): Dual<Rational> =
        dual(
            f = x.f * x.f,
            df = Rational.TWO * x.f * x.df
        )

    /**
     * Cube.
     * ```text
     * (a + bε)³ = a³ + 3a²b ε
     * ```
     */
    fun cube(x: Dual<Rational>): Dual<Rational> =
        dual(
            f = x.f * x.f * x.f,
            df = 3.toRational() * x.f * x.f * x.df
        )

    /**
     *
     */
    fun mobius(
        x: Dual<Rational>,
        a: Rational,
        b: Rational,
        c: Rational,
        d: Rational
    ): Dual<Rational> {
        val denominator = c * x.f + d
        require(denominator != Rational.ZERO) {
            "mobius undefined when c*f + d = 0, got f=${x.f}"
        }
        val numerator = a * x.f + b
        val value = numerator / denominator
        val derivative = (a * d - b * c) / (denominator * denominator)
        return dual(
            f = value,
            df = x.df * derivative
        )
    }
}
