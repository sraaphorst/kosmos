// src/main/kotlin/org/vorpal/kosmos/std/Rational.kt
package org.vorpal.kosmos.std

import java.math.BigInteger

/** Normalized rational n/d with d > 0 and gcd(n,d) = 1. */
@ConsistentCopyVisibility
data class Rational private constructor(val n: BigInteger, val d: BigInteger) {
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
        fun of(n: Long, d: Long) = of(BigInteger.valueOf(n), BigInteger.valueOf(d))
        val zero = of(0, 1)
        val one  = of(1, 1)
    }

    operator fun unaryMinus(): Rational = of(n.negate(), d)
    operator fun plus  (o: Rational): Rational = of(n * o.d + o.n * d, d * o.d)
    operator fun minus (o: Rational): Rational = this + (-o)
    operator fun times (o: Rational): Rational = of(n * o.n, d * o.d)

    fun reciprocal(): Rational {
        require(n != BigInteger.ZERO) { "0 has no multiplicative inverse in a field" }
        return of(d, n)
    }
}