package org.vorpal.kosmos.numberfields.quadratic

import java.math.BigInteger

/**
 * The Eisenstein integers are of the form `a + b * ω`, with `ω^2 + ω + 1 = 0`.
 * ```text
 * ω = (-1 + i sqrt(3))/2 = e^{i 2π/3}.
 * ```
 * Note that:
 * 1. `ω` is a primitive (hence non-real) cube root of unity.
 * 2. They form an equilateral triangular lattice / hexagonal lattice in the complex plane
 *    (in contrast with the Gaussian integers, which form a square lattice).
 * 3. They also form a commutative ring of algebraic integers in the algebraic number field `ℚ(ω)`, the third
 *    cyclotomic field.
 * 4. To see that they are algebraic, note that each EisensteinInt is a root of:
 * ```text
 *  z^2 - (2a - b)z + (a^2 - ab + b^2)
 *  ```
 *  They cannot be created via the Cayley-Dickson construction as they are not a doubling.
 */
data class EisensteinInt(
    val a: BigInteger,
    val b: BigInteger
) {
    companion object {
        val ZERO: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ZERO)
        val ONE: EisensteinInt = EisensteinInt(BigInteger.ONE, BigInteger.ZERO)

        // ω
        val OMEGA: EisensteinInt = EisensteinInt(BigInteger.ZERO, BigInteger.ONE)

        // This is the fundamental relation that defines the Eisenstein integers.
        // ω^2 = -1 - ω
        val OMEGA_SQ: EisensteinInt = EisensteinInt(-BigInteger.ONE, -BigInteger.ONE)
    }
}
