package org.vorpal.kosmos.hypercomplex.complex

import java.math.BigInteger

/**
 * The Eisenstein integers are of the form a + b * ω, with ω^2 + ω + 1 = 0.
 *
 *     ω = (-1 + i sqrt(3))/2 = e^{i 2π/3}.
 *
 * Note that:
 * 1. `ω` is a primitive (hence non-real) cube root of unity.
 * 2. They form a triangular lattice in the complex plane (in contrast with the Gaussian integers,
 *    which form a square lattice).
 * 3. They also form a commutative ring of algebraic integers in the algebraic number field `ℚ(ω)`, the third
 *    cyclotomic field.
 * 4. To see that they are algebraic, note that each EisensteinInt is a root of:
 *
 *     z^2 - (2a - b)z + (a^2 - ab + b^2)
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
