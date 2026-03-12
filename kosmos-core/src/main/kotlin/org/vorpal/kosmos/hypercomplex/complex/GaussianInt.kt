package org.vorpal.kosmos.hypercomplex.complex

import java.math.BigInteger

/**
 * The type for a Gaussian integer, i.e. a [BigInteger] real and complex component.
 *
 * Note that this could also be generated using `CayleyDickson` on a [ZCommutativeRing].
 */
data class GaussianInt(val re: BigInteger, val im: BigInteger) {
    operator fun plus(other: GaussianInt): GaussianInt = GaussianInt(re + other.re, im + other.im)
    operator fun minus(other: GaussianInt): GaussianInt = GaussianInt(re - other.re, im - other.im)
    operator fun times(other: GaussianInt): GaussianInt = GaussianInt(re * other.re - im * other.im, re * other.im + im * other.re)
    operator fun unaryMinus(): GaussianInt = GaussianInt(-re, -im)

    companion object {
        val ZERO = GaussianInt(BigInteger.ZERO, BigInteger.ZERO)
        val ONE = GaussianInt(BigInteger.ONE, BigInteger.ZERO)
    }
}
