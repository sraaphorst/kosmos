package org.vorpal.kosmos.core

import java.math.BigInteger
import kotlin.math.absoluteValue

// ===== GCD Functions =====

/**
 * Calculate the greatest common divisor of two integers.
 */
tailrec fun gcd(first: Int, second: Int): Int =
    if (second == 0) first.absoluteValue else gcd(second, first % second)

/**
 * Calculate the greatest common divisor of multiple integers.
 */
fun gcd(first: Int, second: Int, vararg others: Int): Int =
    others.fold(gcd(first, second)) { acc, num -> gcd(acc, num) }

/**
 * Calculate the greatest common divisor of a collection of integers.
 */
fun Iterable<Int>.gcd(): Int =
    this.reduce { acc, num -> gcd(acc, num) }

/**
 * Calculate the greatest common divisor of two longs.
 */
tailrec fun gcd(first: Long, second: Long): Long =
    if (second == 0L) first.absoluteValue else gcd(second, first % second)

/**
 * Calculate the greatest common divisor of multiple longs.
 */
fun gcd(first: Long, second: Long, vararg others: Long): Long =
    others.fold(gcd(first, second)) { acc, num -> gcd(acc, num) }

/**
 * Calculate the greatest common divisor of a collection of longs.
 */
fun Iterable<Long>.gcd(): Long =
    this.reduce { acc, num -> gcd(acc, num) }

/**
 * Calculate the greatest common divisor of two BigIntegers.
 */
fun gcd(first: BigInteger, second: BigInteger): BigInteger =
    first.gcd(second)

/**
 * Calculate the greatest common divisor of multiple BigIntegers.
 */
fun gcd(first: BigInteger, second: BigInteger, vararg others: BigInteger): BigInteger =
    others.fold(gcd(first, second)) { acc, num -> gcd(acc, num) }

/**
 * Calculate the greatest common divisor of a collection of BigIntegers.
 */
fun Iterable<BigInteger>.gcd(): BigInteger =
    this.reduce { acc, num -> gcd(acc, num) }

// ===== LCM Functions =====

/**
 * Calculate the least common multiple of two integers.
 */
fun lcm(first: Int, second: Int): Int = when {
    first == 0 || second == 0 -> 0
    else -> {
        val firstAbs = first.absoluteValue
        val secondAbs = second.absoluteValue
        val gcdValue = gcd(firstAbs, secondAbs)
        // Use Long arithmetic to avoid overflow, then convert back to Int
        val result = (firstAbs.toLong() / gcdValue) * secondAbs.toLong()
        result.toInt()  // This will wrap around on overflow, maintaining mathematical properties
    }
}

/**
 * Calculate the least common multiple of multiple integers.
 */
fun lcm(first: Int, second: Int, vararg others: Int): Int =
    others.fold(lcm(first, second)) { acc, num -> lcm(acc, num) }

/**
 * Calculate the least common multiple of a collection of integers.
 */
fun Iterable<Int>.lcm(): Int =
    this.reduce { acc, num -> lcm(acc, num) }

/**
 * Calculate the least common multiple of two longs.
 */
fun lcm(first: Long, second: Long): Long = when {
    first == 0L || second == 0L -> 0L
    else -> {
        val firstAbs = first.absoluteValue
        val secondAbs = second.absoluteValue
        (firstAbs / gcd(firstAbs, secondAbs)) * secondAbs
    }
}

/**
 * Calculate the least common multiple of multiple longs.
 */
fun lcm(first: Long, second: Long, vararg others: Long): Long =
    others.fold(lcm(first, second)) { acc, num -> lcm(acc, num) }

/**
 * Calculate the least common multiple of a collection of longs.
 */
fun Iterable<Long>.lcm(): Long =
    this.reduce { acc, num -> lcm(acc, num) }

/**
 * Calculate the least common multiple of two BigIntegers.
 */
fun lcm(first: BigInteger, second: BigInteger): BigInteger = when {
    first == BigInteger.ZERO || second == BigInteger.ZERO -> BigInteger.ZERO
    else -> {
        val firstAbs = first.abs()
        val secondAbs = second.abs()
        (firstAbs / gcd(firstAbs, secondAbs)) * secondAbs
    }
}

/**
 * Calculate the least common multiple of multiple BigIntegers.
 */
fun lcm(first: BigInteger, second: BigInteger, vararg others: BigInteger): BigInteger =
    others.fold(lcm(first, second)) { acc, num -> lcm(acc, num) }

/**
 * Calculate the least common multiple of a collection of BigIntegers.
 */
fun Iterable<BigInteger>.lcm(): BigInteger =
    this.reduce { acc, num -> lcm(acc, num) }

// ===== Extended GCD and Modular Inverse =====

/**
 * Result of extended Euclidean algorithm: a*x + b*y = gcd(a,b)
 */
sealed class GcdResult<T>(val gcd: T, val x: T, val a: T, val y: T, val b: T) {

    protected abstract fun equals(a: T, b: T): Boolean
    protected abstract fun multiply(a: T, b: T): T
    protected abstract fun add(a: T, b: T): T
    protected abstract fun mod(a: T, b: T): T
    protected abstract fun isOne(value: T): Boolean

    /**
     * Verify that a*x + b*y = gcd
     */
    fun verify(): Boolean =
        equals(add(multiply(a, x), multiply(b, y)), gcd)

    /**
     * Calculate the modular inverse of 'a' modulo 'b'.
     * Requires gcd(a, b) = 1.
     */
    fun modInverse(): T {
        require(isOne(gcd)) { "$a has no modular inverse mod $b (gcd = $gcd)" }
        return mod(x, b)
    }
}

/**
 * GcdResult implementation for Int
 */
class IntGcdResult(gcd: Int, x: Int, a: Int, y: Int, b: Int) : GcdResult<Int>(gcd, x, a, y, b) {
    override fun equals(a: Int, b: Int): Boolean = a == b
    override fun multiply(a: Int, b: Int): Int = a * b
    override fun add(a: Int, b: Int): Int = a + b
    override fun mod(a: Int, b: Int): Int = a.mod(b)
    override fun isOne(value: Int): Boolean = value == 1
}

/**
 * GcdResult implementation for Long
 */
class LongGcdResult(gcd: Long, x: Long, a: Long, y: Long, b: Long) : GcdResult<Long>(gcd, x, a, y, b) {
    override fun equals(a: Long, b: Long): Boolean = a == b
    override fun multiply(a: Long, b: Long): Long = a * b
    override fun add(a: Long, b: Long): Long = a + b
    override fun mod(a: Long, b: Long): Long = a.mod(b)
    override fun isOne(value: Long): Boolean = value == 1L
}

/**
 * GcdResult implementation for BigInteger
 */
class BigIntegerGcdResult(gcd: BigInteger, x: BigInteger, a: BigInteger, y: BigInteger, b: BigInteger) : GcdResult<BigInteger>(gcd, x, a, y, b) {
    override fun equals(a: BigInteger, b: BigInteger): Boolean = a == b
    override fun multiply(a: BigInteger, b: BigInteger): BigInteger = a * b
    override fun add(a: BigInteger, b: BigInteger): BigInteger = a + b
    override fun mod(a: BigInteger, b: BigInteger): BigInteger = a.mod(b)
    override fun isOne(value: BigInteger): Boolean = value == BigInteger.ONE
}

/**
 * Extended Euclidean algorithm for integers.
 * Returns x, y such that: first*x + second*y = gcd(first, second)
 */
fun extendedGcd(first: Int, second: Int): IntGcdResult {
    tailrec fun aux(r0: Int = first, r1: Int = second,
                    s0: Int = 1, s1: Int = 0,
                    t0: Int = 0, t1: Int = 1): Triple<Int, Int, Int> =
        if (r1 == 0) Triple(r0, s0, t0)
        else {
            val q = r0 / r1
            aux(r1, r0 - q * r1, s1, s0 - q * s1, t1, t0 - q * t1)
        }

    val (g, x, y) = aux()
    // Ensure GCD is positive to match regular gcd function
    if (g < 0) {
        return IntGcdResult(-g, -x, first, -y, second)
    }
    return IntGcdResult(g, x, first, y, second)
}

/**
 * Extended Euclidean algorithm for longs.
 */
fun extendedGcd(first: Long, second: Long): LongGcdResult {
    tailrec fun aux(r0: Long = first, r1: Long = second,
                    s0: Long = 1L, s1: Long = 0L,
                    t0: Long = 0L, t1: Long = 1L): Triple<Long, Long, Long> =
        if (r1 == 0L) Triple(r0, s0, t0)
        else {
            val q = r0 / r1
            aux(r1, r0 - q * r1, s1, s0 - q * s1, t1, t0 - q * t1)
        }

    val (g, x, y) = aux()
    // Ensure GCD is positive to match regular gcd function
    if (g < 0L) {
        return LongGcdResult(-g, -x, first, -y, second)
    }
    return LongGcdResult(g, x, first, y, second)
}

/**
 * Extended Euclidean algorithm for BigIntegers.
 */
fun extendedGcd(first: BigInteger, second: BigInteger): BigIntegerGcdResult {
    tailrec fun aux(r0: BigInteger = first, r1: BigInteger = second,
                    s0: BigInteger = BigInteger.ONE, s1: BigInteger = BigInteger.ZERO,
                    t0: BigInteger = BigInteger.ZERO, t1: BigInteger = BigInteger.ONE
    ): Triple<BigInteger, BigInteger, BigInteger> =
        if (r1 == BigInteger.ZERO) Triple(r0, s0, t0)
        else {
            val q = r0 / r1
            aux(r1, r0 - q * r1, s1, s0 - q * s1, t1, t0 - q * t1)
        }

    val (g, x, y) = aux()
    // Ensure GCD is positive to match regular gcd function
    if (g < BigInteger.ZERO) {
        return BigIntegerGcdResult(g.abs(), x.negate(), first, y.negate(), second)
    }
    return BigIntegerGcdResult(g, x, first, y, second)
}


// ===== Convenience Functions =====

/**
 * Calculate modular inverse directly
 */
fun Int.modInverse(modulus: Int): Int = extendedGcd(this, modulus).modInverse()
fun Long.modInverse(modulus: Long): Long = extendedGcd(this, modulus).modInverse()

// Java has a BigInteger.modInverse already, so this is redundant.
//fun BigInteger.modInverse(modulus: BigInteger): BigInteger = extendedGcd(this, modulus).modInverse()