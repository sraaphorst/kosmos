package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.core.rational.Rational

/**
 * This carrier is mathematically equivalent to a Cayley-Dickson doubling of the rationals, though it is currently
 * represented as a named concrete type.
 */
data class GaussianRat(val re: Rational, val im: Rational) {
    companion object {
        val ZERO = GaussianRat(Rational.ZERO, Rational.ZERO)
        val ONE = GaussianRat(Rational.ONE, Rational.ZERO)
        val I = GaussianRat(Rational.ZERO, Rational.ONE)
    }
}
