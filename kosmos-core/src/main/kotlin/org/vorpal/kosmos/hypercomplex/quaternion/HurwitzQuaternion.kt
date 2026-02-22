package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger


/**
 * A Hurwitz quaternion is a quaternion:
 * ```
 * q = a + bi + cj + dk
 * ```
 * where either
 * - `a, b, c, d ∈ ℤ` (Lipschitz case); or
 * - `a, b, c, d ∈ ℤ + 1/2` (all half-integers) with the same parity pattern
 *   (equivalently: `a, b, c, d` are all half-integers and `a + b + c + d ∈ ℤ`).
 *
 * This set is the unique “nice” maximal order inside the rational quaternions.
 */
data class HurwitzQuaternion(
    val a: Rational,
    val b: Rational,
    val c: Rational,
    val d: Rational
) {
    init {
        require(isHurwitz(a, b, c, d)) {
            "Not a Hurwitz quaternion: coordinates must be all integers or all half-integers with even sum"
        }
    }

    /**
     * Hurwitz quaternions are closed under addition, subtraction, negation, and multiplication.
     */
    operator fun plus(other: HurwitzQuaternion): HurwitzQuaternion =
        HurwitzQuaternion(a + other.a, b + other.b, c + other.c, d + other.d)
    operator fun minus(other: HurwitzQuaternion): HurwitzQuaternion =
        HurwitzQuaternion(a - other.a, b - other.b, c - other.c, d - other.d)
    operator fun unaryMinus(): HurwitzQuaternion = HurwitzQuaternion(-a, -b, -c, -d)
    operator fun times(other: HurwitzQuaternion): HurwitzQuaternion =
        HurwitzQuaternion(a * other.a - b * other.b - c * other.c - d * other.d,
            a * other.b + b * other.a + c * other.d - d * other.c,
            a * other.c - b * other.d + c * other.a + d * other.b,
            a * other.d + b * other.c - c * other.b + d * other.a)

    companion object {
        /**
         * A quaternion (a, b, c, d) is Hurwitz iff:
         * - `2a`, `2b`, `2c`, `2d` are all integers
         * - `2a = 2b = 2c = 2d (mod 2)`
         */
        fun isHurwitz(a: Rational, b: Rational, c: Rational, d: Rational): Boolean {
            val doubled = listOf(a, b, c, d).map { it * BigInteger.TWO }
            if (!doubled.all(Rational::isInteger)) return false
            val p0 = doubled.first().numerator.mod(BigInteger.TWO)
            return doubled.all { it.numerator.mod(BigInteger.TWO) == p0 }
        }

        val ZERO = HurwitzQuaternion(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO)
        val ONE = HurwitzQuaternion(Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ZERO)
    }
}
