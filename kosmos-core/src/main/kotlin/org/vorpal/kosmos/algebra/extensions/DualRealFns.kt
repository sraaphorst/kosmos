package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.core.math.Real
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * Elementary functions on dual numbers over Real.
 *
 * For x = a + bε with ε² = 0:
 *   f(x) = f(a) + b f'(a) ε
 */
object DualRealFns {
    fun sin(x: Dual<Real>): Dual<Real> =
        Dual(
            a = sin(x.a),
            b = x.b * cos(x.a),
        )

    fun cos(x: Dual<Real>): Dual<Real> =
        Dual(
            a = cos(x.a),
            b = -x.b * sin(x.a),
        )

    fun tan(x: Dual<Real>): Dual<Real> {
        val ca = cos(x.a)
        val sec2 = 1.0 / (ca * ca)
        return Dual(
            a = tan(x.a),
            b = x.b * sec2,
        )
    }

    fun exp(x: Dual<Real>): Dual<Real> {
        val ea = exp(x.a)
        return Dual(
            a = ea,
            b = x.b * ea,
        )
    }

    fun log(x: Dual<Real>): Dual<Real> {
        require(x.a > 0.0) { "log requires positive real part, got ${x.a}" }
        return Dual(
            a = ln(x.a),
            b = x.b / x.a,
        )
    }

    fun sqrt(x: Dual<Real>): Dual<Real> {
        require(x.a >= 0.0) { "sqrt requires nonnegative real part, got ${x.a}" }
        val sa = sqrt(x.a)
        return Dual(
            a = sa,
            b = x.b / (2.0 * sa),
        )
    }

    // Inverse trig (domain restrictions on real part apply)
    fun asin(x: Dual<Real>): Dual<Real> {
        require(x.a in -1.0..1.0) { "asin requires real part in [-1,1], got ${x.a}" }
        val denom = sqrt(1.0 - x.a * x.a)
        return Dual(
            a = asin(x.a),
            b = x.b / denom,
        )
    }

    fun acos(x: Dual<Real>): Dual<Real> {
        require(x.a in -1.0..1.0) { "acos requires real part in [-1,1], got ${x.a}" }
        val denom = sqrt(1.0 - x.a * x.a)
        return Dual(
            a = acos(x.a),
            b = -x.b / denom,
        )
    }

    fun atan(x: Dual<Real>): Dual<Real> =
        Dual(
            a = atan(x.a),
            b = x.b / (1.0 + x.a * x.a),
        )

    fun sinh(x: Dual<Real>): Dual<Real> =
        Dual(
            a = sinh(x.a),
            b = x.b * cosh(x.a),
        )

    fun cosh(x: Dual<Real>): Dual<Real> =
        Dual(
            a = cosh(x.a),
            b = x.b * sinh(x.a),
        )

    fun tanh(x: Dual<Real>): Dual<Real> {
        val ta = tanh(x.a)
        val sech2 = 1.0 - ta * ta
        return Dual(
            a = ta,
            b = x.b * sech2,
        )
    }

    fun pow(x: Dual<Real>, n: Int): Dual<Real> {
        require(n >= 0) { "pow requires n >= 0" }

        val aPow = x.a.powInt(n)
        val bPart = if (n == 0) 0.0 else n * x.b * x.a.powInt(n - 1)

        return Dual(
            a = aPow,
            b = bPart,
        )
    }

    private fun Real.powInt(n: Int): Real {
        require(n >= 0) { "n must be >= 0" }

        var base = this
        var exp = n
        var acc = 1.0

        while (exp > 0) {
            if ((exp and 1) == 1) acc *= base
            base *= base
            exp = exp ushr 1
        }

        return acc
    }
}
