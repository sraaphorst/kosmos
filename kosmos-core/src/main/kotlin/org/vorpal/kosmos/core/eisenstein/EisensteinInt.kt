package org.vorpal.kosmos.core.eisenstein

import java.math.BigInteger

/**
 * Einstein integer a + b * ω with ω^2 + ω + 1 = 0.
 */
data class EisensteinInt(
    val a: BigInteger,
    val b: BigInteger
) {
    operator fun plus(other: EisensteinInt): EisensteinInt = EisensteinInt(a + other.a, b + other.b)
    operator fun minus(other: EisensteinInt): EisensteinInt = EisensteinInt(a - other.a, b - other.b)
    operator fun times(other: EisensteinInt): EisensteinInt = EisensteinInt(
        a * other.a - b * other.b, a * other.b + b * other.a - b * other.b
    )
    operator fun unaryMinus(): EisensteinInt = EisensteinInt(-a, -b)

    companion object {
        val ZERO: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ZERO)
        val ONE: EisensteinInt = EisensteinInt(BigInteger.ONE, BigInteger.ZERO)

        // ω
        val OMEGA: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ONE)

        // ω^2 = -1 - ω
        val OMEGA_SQ: EisensteinInt = EisensteinInt(-BigInteger.ONE, -BigInteger.ONE)
    }
}
