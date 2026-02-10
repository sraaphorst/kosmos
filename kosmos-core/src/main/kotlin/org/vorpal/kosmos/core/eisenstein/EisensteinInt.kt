package org.vorpal.kosmos.core.eisenstein

import java.math.BigInteger

/**
 * Einstein integer a + b * ω with ω^2 + ω + 1 = 0.
 */
data class EisensteinInt(
    val a: BigInteger,
    val b: BigInteger
) {
    companion object {
        val zero: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ZERO)
        val one: EisensteinInt = EisensteinInt(BigInteger.ONE, BigInteger.ZERO)

        // ω
        val omega: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ONE)

        // ω^2 = -1 - ω
        val omegaSq: EisensteinInt = EisensteinInt(-BigInteger.ONE, -BigInteger.ONE)
    }
}
