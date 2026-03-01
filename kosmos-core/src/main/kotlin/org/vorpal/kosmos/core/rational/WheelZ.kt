package org.vorpal.kosmos.core.rational

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.functional.datastructures.Option
import java.math.BigInteger

/**
 * This is similar to Rational, but with support for an infinity and a bottom element.
 * WheelZ represents a rational number in the form of `n/d`, where `n` and `d` are BigIntegers.
 * It supports operations like addition, subtraction, multiplication, and division.
 * - infinity is represented as `1/0`;
 * - zero is represented as `0/1`; and
 * - bottom is represented as `0/0`.
 *
 * Like Rational. [WheelZ] is converted to reduced form on construction, i.e.:
 * - The numerator and denominator are reduced modulo their greatest common divisor;
 * - The denominator is always positive. The sign is on the numerator.
 */
@ConsistentCopyVisibility
data class WheelZ private constructor(
    val n: BigInteger,
    val d: BigInteger
) : Comparable<WheelZ> {
    val isBottom: Boolean
        get() = d == BigInteger.ZERO && n == BigInteger.ZERO
    val isInfinite: Boolean
        get() = d == BigInteger.ZERO && n != BigInteger.ZERO
    val isFinite: Boolean
        get() = d != BigInteger.ZERO
    val signum: Int
        get() = n.signum()
    val isZero: Boolean
        get() = n == BigInteger.ZERO
    val isPositive: Boolean
        get() = n.signum() > 0
    val isNegative: Boolean
        get() = n.signum() < 0

    operator fun unaryMinus(): WheelZ = when {
        isBottom -> BOTTOM
        isInfinite -> INF
        else -> of(n.negate(), d)
    }

    fun inv(): WheelZ =
        if (isBottom) BOTTOM else of(d, n)

    operator fun plus(other: WheelZ): WheelZ {
        if (isBottom || other.isBottom) return BOTTOM

        val num = n * other.d + d * other.n
        val den = d * other.d
        return of(num, den)
    }

    operator fun minus(other: WheelZ): WheelZ =
        this + (-other)

    operator fun times(other: WheelZ): WheelZ {
        if (isBottom || other.isBottom) return BOTTOM
        return of(n * other.n, d * other.d)
    }

    operator fun div(other: WheelZ): WheelZ =
        this * other.inv()

    fun compareToOrNull(other: WheelZ): Int? = when {
        isBottom || other.isBottom -> null
        isInfinite -> if (other.isInfinite) 0 else 1
        other.isInfinite -> -1
        else -> (n * other.d).compareTo(other.n * d)
    }

    /**
     * This compareTo will fail if either operand is bottom.
     */
    override fun compareTo(other: WheelZ): Int =
        compareToOrNull(other) ?: throw IllegalArgumentException("Cannot compare bottom values")

    /**
     * Absolute value: finite values use |n|/d, -∞ maps to +∞, and ⊥ stays ⊥.
     */
    fun abs(): WheelZ =
        of(n.abs(), d)

    fun pow(exp: Int): WheelZ = when {
        isBottom -> BOTTOM
        exp == 0 -> ONE
        exp < 0 -> inv().pow(-exp)
        else -> of(n.pow(exp), d.pow(exp))
    }

    fun toRational(): Option<Rational> = when {
        isBottom || isInfinite -> Option.None
        else -> Option.Some(Rational.of(n, d))
    }

    override fun toString(): String = when {
        isBottom -> Symbols.BOTTOM
        isInfinite -> Symbols.INFINITY
        d == BigInteger.ONE -> n.toString()
        else -> "$n/$d"
    }

    companion object {
        val BOTTOM = WheelZ(BigInteger.ZERO, BigInteger.ZERO)
        val ZERO = of(BigInteger.ZERO, BigInteger.ONE)
        val ONE = of(BigInteger.ONE, BigInteger.ONE)
        val INF = of(BigInteger.ONE, BigInteger.ZERO) // 1/0

        fun of(n: Int, d: Int): WheelZ =
            of(n.toBigInteger(), d.toBigInteger())

        fun of(n: BigInteger, d: BigInteger): WheelZ {
            // bottom
            if (d == BigInteger.ZERO && n == BigInteger.ZERO) return BOTTOM

            // infinities: canonicalize to ±1/0
            if (d == BigInteger.ZERO) {
                return WheelZ(BigInteger.ONE, BigInteger.ZERO)
            }

            // finite: reduce like Rational, enforce d > 0
            val sign = if (d.signum() < 0) BigInteger.valueOf(-1) else BigInteger.ONE
            val nn0 = n * sign
            val dd0 = d.abs()

            if (nn0 == BigInteger.ZERO) {
                return WheelZ(BigInteger.ZERO, BigInteger.ONE)
            }

            val g = nn0.gcd(dd0)
            return WheelZ(nn0 / g, dd0 / g)
        }
    }
}

fun main() {
    // All of these should evaluate to true.
    println(WheelZ.ONE / WheelZ.ZERO == WheelZ.INF)
    println(WheelZ.of(-2, 3) / WheelZ.ZERO == WheelZ.INF)
    println(WheelZ.ZERO / WheelZ.ZERO == WheelZ.BOTTOM)
    println(WheelZ.INF + WheelZ.INF == WheelZ.BOTTOM)
    println(WheelZ.ZERO * WheelZ.INF == WheelZ.BOTTOM)
    println(WheelZ.of(5, 2) + WheelZ.INF == WheelZ.INF)
}
