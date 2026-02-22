package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * The type for a Gaussian rational extension, i.e. a [Rational] real and imaginary component.
 *
 * Note that this could also be generated using `CayleyDickson` on a [RationalStarField].
 */
data class GaussianRat(val re: Rational, val im: Rational) {
    operator fun plus(other: GaussianRat): GaussianRat = GaussianRat(re + other.re, im + other.im)
    operator fun minus(other: GaussianRat): GaussianRat = GaussianRat(re - other.re, im - other.im)
    operator fun times(other: GaussianRat): GaussianRat = GaussianRat(re * other.re - im * other.im, re * other.im + im * other.re)
    operator fun div(other: GaussianRat): GaussianRat = GaussianRat((re * other.re + im * other.im) / (other.re * other.re + other.im * other.im), (im * other.re - re * other.im) / (other.re * other.re + other.im * other.im))
    operator fun unaryMinus(): GaussianRat = GaussianRat(-re, -im)

    /**
     * The reciprocal of the Gaussian rational a/b + i c/d is:
     * abd^2 / (a^2 d^2 + c^2 b^2) + i b^2 c d / (a^2 d^2 + c^2 b^2).
     */
    fun reciprocal(): GaussianRat {
        val (a, b) = re
        val (c, d) = im
        val newDenom = a * a * d * d + c * c * b * b
        require(newDenom != BigInteger.ZERO) { "The reciprocal of $this is undefined" }
        return GaussianRat(Rational.of(a * b * d * d, newDenom), Rational.of(- b * b * c * d, newDenom))
    }

    companion object {
        val ZERO = GaussianRat(Rational.ZERO, Rational.ZERO)
        val ONE = GaussianRat(Rational.ONE, Rational.ZERO)
        val I = GaussianRat(Rational.ZERO, Rational.ONE)
    }
}
