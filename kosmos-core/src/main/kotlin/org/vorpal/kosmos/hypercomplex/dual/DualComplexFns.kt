package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras.powInt

/**
 * Elementary algebraic functions on dual numbers over [Complex].
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
 * This file starts with algebraic functions that do not require a separate complex-analytic
 * function library. Once the complex elementary functions (`exp`, `log`, `sin`, `cos`, etc.)
 * are available in a stable form, this file can be extended naturally.
 */
object DualComplexFns {
    private val field = ComplexAlgebras.ComplexField

    /**
     * Reciprocal.
     * ```text
     * (a + bε)^(-1) = a^(-1) - a^(-2) b ε
     * ```
     * Requires `a != 0`.
     */
    fun inv(x: Dual<Complex>): Dual<Complex> {
        require(x.a != field.zero) { "inv requires nonzero real part, got ${x.a}" }

        val invA = field.reciprocal(x.a)
        val invA2 = field.mul(invA, invA)
        val epsPart = field.mul(field.add.inverse(x.b), invA2)

        return Dual(
            a = invA,
            b = epsPart
        )
    }

    /**
     * Integer power with nonnegative exponent.
     * ```text
     * (a + bε)^n = a^n + n a^(n-1) b ε
     * ```
     * Requires `n >= 0`.
     */
    fun pow(x: Dual<Complex>, n: Int): Dual<Complex> {
        require(n >= 0) { "pow requires n >= 0" }

        val aPow = x.a.powInt(n)
        val bPart =
            if (n == 0) field.zero
            else {
                val nAsComplex = field.fromBigInt(n.toBigInteger())
                val scalar = field.mul(nAsComplex, x.a.powInt(n - 1))
                field.mul(scalar, x.b)
            }

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
    fun square(x: Dual<Complex>): Dual<Complex> {
        val two = field.fromBigInt(2.toBigInteger())
        val a2 = field.mul(x.a, x.a)
        val ab = field.mul(x.a, x.b)

        return Dual(
            a = a2,
            b = field.mul(two, ab)
        )
    }

    /**
     * Cube.
     * ```text
     * (a + bε)³ = a³ + 3a²b ε
     * ```
     */
    fun cube(x: Dual<Complex>): Dual<Complex> {
        val three = field.fromBigInt(3.toBigInteger())
        val a2 = field.mul(x.a, x.a)
        val a3 = field.mul(a2, x.a)
        val a2b = field.mul(a2, x.b)

        return Dual(
            a = a3,
            b = field.mul(three, a2b)
        )
    }
}
