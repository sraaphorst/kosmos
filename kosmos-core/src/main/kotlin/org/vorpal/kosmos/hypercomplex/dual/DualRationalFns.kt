package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.core.rational.Rational

/**
 * Elementary algebraic functions on dual numbers over [Rational].
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
        require(x.a != Rational.ZERO) { "inv requires nonzero real part, got ${x.a}" }

        val invA = x.a.reciprocal()
        val invA2 = invA * invA

        return Dual(
            a = invA,
            b = -x.b * invA2
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
        require(n >= 0) { "pow requires n >= 0" }

        val aPow = x.a.powInt(n)
        val bPart =
            if (n == 0) Rational.ZERO
            else Rational.of(n.toBigInteger()) * x.b * x.a.powInt(n - 1)

        return Dual(
            a = aPow,
            b = bPart
        )
    }

    /**
     * Square.
     * ```text
     * (a + bε)² = a² + 2ab ε
     * ```
     */
    fun square(x: Dual<Rational>): Dual<Rational> =
        Dual(
            a = x.a * x.a,
            b = Rational.TWO * x.a * x.b
        )

    /**
     * Cube.
     * ```text
     * (a + bε)³ = a³ + 3a²b ε
     * ```
     */
    fun cube(x: Dual<Rational>): Dual<Rational> =
        Dual(
            a = x.a * x.a * x.a,
            b = Rational.of(3.toBigInteger()) * x.a * x.a * x.b
        )

    private fun Rational.powInt(n: Int): Rational {
        require(n >= 0) { "n must be >= 0" }

        var base = this
        var exp = n
        var acc = Rational.ONE

        while (exp > 0) {
            if ((exp and 1) == 1) acc *= base
            base *= base
            exp = exp ushr 1
        }

        return acc
    }
}