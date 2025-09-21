package org.vorpal.kosmos.modular

import java.math.BigInteger
import kotlin.math.absoluteValue

tailrec fun gcd(a: Int, b: Int): Int =
    if (b == 0) a.absoluteValue else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int =
    (a / gcd(a, b)) * b

fun gcd(a: BigInteger, b: BigInteger): BigInteger = a.gcd(b)

fun lcm(a: BigInteger, b: BigInteger): BigInteger =
    (a / a.gcd(b)) * b

data class GcdResult<T>(val gcd: T, val x: T, val a: T, val y: T, val b: T) {
    fun verify(multiply: (T, T) -> T, add: (T, T) -> T, equals: (T, T) -> Boolean): Boolean =
        equals(add(multiply(a, x), multiply(b, y)), gcd)
}

fun extendedGcd(a: Int, b: Int): GcdResult<Int> {
    tailrec fun aux(r0: Int = a, r1: Int = b,
                    s0: Int = 1, s1: Int = 0,
                    t0: Int = 0, t1: Int = 1): Triple<Int, Int, Int> =
        if (r1 == 0) Triple(r0, s0, t0)
        else {
            val q = r0 / r1
            aux(r1, r0 - q * r1, s1, s0 - q * s1, t1, t0 - q * t1)
        }
    val (g, x, y) = aux()
    return GcdResult(g, x, a, y, b)
}

fun extendedGcd(a: BigInteger, b: BigInteger): GcdResult<BigInteger> {
    tailrec fun aux(r0: BigInteger = a, r1: BigInteger = b,
                    s0: BigInteger = BigInteger.ONE, s1: BigInteger = BigInteger.ZERO,
                    t0: BigInteger = BigInteger.ZERO, t1: BigInteger = BigInteger.ONE
    ): Triple<BigInteger, BigInteger, BigInteger> =
        if (r1 == BigInteger.ZERO) Triple(r0, s0, t0)
        else {
            val q = r0 / r1
            aux(r1, r0 - q * r1, s1, s0 - q * s1, t1, t0 - q * t1)
        }
    val (g, x, y) = aux()
    return GcdResult(g, x, a, y, b)
}

fun GcdResult<Int>.modInverse(modulus: Int): Int {
    require(gcd == 1) { "$a has no inverse mod $modulus" }
    return x.mod(modulus)
}

fun GcdResult<BigInteger>.modInverse(modulus: BigInteger): BigInteger {
    require(gcd == BigInteger.ONE) { "$a has no inverse mod $modulus" }
    return x.mod(modulus)
}
