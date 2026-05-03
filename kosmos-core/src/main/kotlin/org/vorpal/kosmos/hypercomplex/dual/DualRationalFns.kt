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
     * Reciprocal. We want to show thatL
     * ```text
     * (a + bε)^(-1) = 1/a - (b/a^2)ε
     * ```
     * Proving this is an exercise which involves factoring out an `a` before exponentiation:
     * ```text
     * (a(1 + (b/a)ε))^(-1)
     * ```
     * We can prove:
     * ```text
     * (1 + tε)(1 - tε) = 1 - t^2ε^2 = 1
     * ```
     * Also:
     * ```text
     * = 1 / (1 + tε)
     * = (1 / (1 + tε)) * ((1 - tε)/(1 - tε))
     * = (1 - tε) / ((1 + tε)(1 - tε))
     * = 1 - tε
     * ```
     *
     * Equivalently:
     * ```text
     * (a * (1 + (b/a)ε))^(-1) // Factor out an a
     * = a^(-1) * (1 + (b/a)ε)^(-1) // From above: t = b/a
     * = a^(-1) * (1 - (b/a)ε)
     * = 1/a - (b/a^2)ε
     * ```
     * Thus, we have the following restriction:
     *
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
     * Möbius / linear fractional transformation.
     *
     * Applies the rational function
     * ```text
     * M(x) = (a x + b) / (c x + d)
     * ```
     * to a dual rational number.
     *
     * If
     * ```text
     * x = u + vε
     * ```
     * then
     * ```text
     * M(x) = M(u) + v M'(u) ε
     * ```
     * where
     * ```text
     * M'(u) = (ad - bc) / (cu + d)².
     * ```
     *
     * Therefore:
     * ```text
     * M(u + vε)
     *   = (au + b) / (cu + d)
     *     + v (ad - bc) / (cu + d)² ε.
     * ```
     *
     * Requires `c * x.f + d != 0`.
     *
     * This is distinct from the number-theoretic Möbius function `μ(n)`.
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
