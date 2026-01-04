package org.vorpal.kosmos.std

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/** Normalized rational n/d with d > 0 and gcd(n,d) = 1. */
@ConsistentCopyVisibility
data class Rational private constructor(val n: BigInteger, val d: BigInteger): Comparable<Rational> {
    companion object {
        fun of(n: BigInteger, d: BigInteger): Rational {
            require(d != BigInteger.ZERO) { "denominator must be nonzero" }
            // move sign to numerator, make denominator positive
            val sign = if (d.signum() < 0) BigInteger.valueOf(-1) else BigInteger.ONE
            val nn = n * sign
            val dd = d.abs()
            val g  = nn.gcd(dd)
            return Rational(nn / g, dd / g)
        }

        fun of(n: Int, d: Int) = of(n.toBigInteger(), d.toBigInteger())
        fun of(n: Long, d: Long) = of(n.toBigInteger(), d.toBigInteger())
        fun of(n: Int) = of(n.toBigInteger(), BigInteger.ONE)
        fun of(n: Long) = of(n.toBigInteger(), BigInteger.ONE)
        fun of(n: BigInteger) = of(n, BigInteger.ONE)

        val ZERO = of(0, 1)
        val ONE  = of(1, 1)
    }

    // Note: all operators that do not directly call of(...) delegate to ones that do to reduce and normalize.
    operator fun unaryMinus(): Rational = of(n.negate(), d)
    operator fun plus  (o: Rational): Rational = of(n * o.d + o.n * d, d * o.d)
    operator fun minus (o: Rational): Rational = this + (-o)
    operator fun times (o: Rational): Rational = of(n * o.n, d * o.d)
    operator fun div   (o: Rational): Rational = this * o.reciprocal()

    /**
     * Remainder of this rational modulo [modulus] where [modulus] is positive is defined as:
     *
     * `this - modulus * floor(this / modulus)`.
     *
     * The result lies in `[0, modulus)`.
     */
    operator fun rem   (modulus: Rational): Rational {
        require(modulus.isPositive) { "Rational modulus must be positive, was: $modulus" }
        val q = (this / modulus).floor()
        return this - modulus * q.toRational()
    }

    fun reciprocal(): Rational {
        require(n != BigInteger.ZERO) { "0 has no multiplicative inverse in a field" }
        return of(d, n)
    }

    // Absolute value
    fun abs(): Rational = of(n.abs(), d)
    val absoluteValue: Rational
        get() = abs()

    // Power
    fun pow(exp: Int): Rational =
        if (exp >= 0) of(n.pow(exp), d.pow(exp)) else reciprocal().pow(-exp)

    // To be able to implement Comparable<Rational>.
    override operator fun compareTo(other: Rational): Int = (n * other.d).compareTo(other.n * d)

    override fun toString(): String =
        if (d == BigInteger.ONE) n.toString()
        else "$n/$d"

    // Type converters:
    fun toBigDecimal(scale: Int = 20): BigDecimal =
        n.toBigDecimal().divide(d.toBigDecimal(), scale, RoundingMode.HALF_EVEN)
    fun toReal(): Real = n.toReal() / d.toReal()
    fun toFloat(): Float = n.toFloat() / d.toFloat()

    @Deprecated("use toReal() instead",
        ReplaceWith("toReal()", imports = ["org.vorpal.kosmos.core.math.Real"]),
        level = DeprecationLevel.ERROR)
    fun toDouble(): Double = n.toDouble() / d.toDouble()

    val isInteger: Boolean
        get() = d == BigInteger.ONE

    val isZero: Boolean
        get() = n == BigInteger.ZERO

    val isPositive: Boolean
        get() = n.signum() > 0

    val isNegative: Boolean
        get() = n.signum() < 0

    val signum: Int
        get() = n.signum()

    /** Floor operation - greatest integer ≤ this rational */
    fun floor(): BigInteger {
        val (q, r) = n.divideAndRemainder(d)
        return if (r.signum() == 0 || n.signum() >= 0) q else q - BigInteger.ONE
    }

    /** Ceiling operation - smallest integer ≥ this rational */
    fun ceil(): BigInteger {
        val (q, r) = n.divideAndRemainder(d)
        return if (r.signum() == 0 || n.signum() <= 0) q else q + BigInteger.ONE
    }

    /**
     * Provides the fractional part of this [Rational], i.e. the part in `[0, 1)`.
     */
    fun frac(): Rational = this % ONE

    /**
     * Return the whole number part of this [Rational], i.e. the part >= 1 or <= -1.
     * Note that this includes the sign.
     */
    fun whole(): BigInteger = signum.toBigInteger() * n.abs() / d

    /** Arithmetic operations with integers (avoiding conversion) */
    operator fun plus(other: Int): Rational = this + other.toRational()
    operator fun plus(other: Long): Rational = this + other.toRational()
    operator fun plus(other: BigInteger): Rational = this + other.toRational()
    operator fun minus(other: Int): Rational = this - other.toRational()
    operator fun minus(other: Long): Rational = this - other.toRational()
    operator fun minus(other: BigInteger): Rational = this - other.toRational()
    operator fun times(other: Int): Rational = this * other.toRational()
    operator fun times(other: Long): Rational = this * other.toRational()
    operator fun times(other: BigInteger): Rational = this * other.toRational()
    operator fun div(other: Int): Rational = this / other.toRational()
    operator fun div(other: Long): Rational = this / other.toRational()
    operator fun div(other: BigInteger): Rational = this / other.toRational()
}

/** Parse rational from string formats like "3/4", "5", "-2/7" */
fun String.toRational(): Rational {
    val trimmed = trim()
    return when {
        '/' in trimmed -> {
            val parts = trimmed.split('/')
            require(parts.size == 2) { "Invalid rational format: $this" }
            Rational.of(BigInteger(parts[0].trim()), BigInteger(parts[1].trim()))
        }
        else -> Rational.of(BigInteger(trimmed))
    }
}

// Pimp existing types to be able to convert to Rational.
fun Int.toRational(): Rational = Rational.of(this.toBigInteger(), BigInteger.ONE)
fun Long.toRational(): Rational = Rational.of(this.toBigInteger(), BigInteger.ONE)
fun BigInteger.toRational(): Rational = Rational.of(this, BigInteger.ONE)
fun Short.toRational(): Rational = Rational.of(this.toInt().toBigInteger(), BigInteger.ONE)
fun Byte.toRational(): Rational  = Rational.of(this.toInt().toBigInteger(), BigInteger.ONE)
fun UInt.toRational(): Rational  = Rational.of(this.toLong().toBigInteger(), BigInteger.ONE)
fun ULong.toRational(): Rational = Rational.of(this.toLong().toBigInteger(), BigInteger.ONE)

// Extension operators for left-hand integer operations
operator fun Int.plus(rational: Rational): Rational = this.toRational() + rational
operator fun Int.minus(rational: Rational): Rational = this.toRational() - rational
operator fun Int.times(rational: Rational): Rational = this.toRational() * rational
operator fun Int.div(rational: Rational): Rational = this.toRational() / rational

operator fun Long.plus(rational: Rational): Rational = this.toRational() + rational
operator fun Long.minus(rational: Rational): Rational = this.toRational() - rational
operator fun Long.times(rational: Rational): Rational = this.toRational() * rational
operator fun Long.div(rational: Rational): Rational = this.toRational() / rational

operator fun BigInteger.plus(rational: Rational): Rational = this.toRational() + rational
operator fun BigInteger.minus(rational: Rational): Rational = this.toRational() - rational
operator fun BigInteger.times(rational: Rational): Rational = this.toRational() * rational
operator fun BigInteger.div(rational: Rational): Rational = this.toRational() / rational