package org.vorpal.kosmos.numberfields.quadratic

import java.math.BigInteger

/**
 * This carrier is mathematically equivalent to a Cayley-Dickson doubling of the integers, though it is currently
 * represented as a named concrete type.
 */
data class GaussianInt(val re: BigInteger, val im: BigInteger) {
    companion object {
        val ZERO = GaussianInt(BigInteger.ZERO, BigInteger.ZERO)
        val ONE = GaussianInt(BigInteger.ONE, BigInteger.ZERO)
    }
}
