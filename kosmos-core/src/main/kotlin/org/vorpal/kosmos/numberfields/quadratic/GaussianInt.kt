package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.hypercomplex.complex.Complex
import java.math.BigInteger

/**
 * A Gaussian integer `re + im·i ∈ ℤ[i]`: a [Complex] number with integer coefficients.
 *
 * `ℤ[i] = ℤ[x]/(x² + 1)` is the Cayley-Dickson doubling of ℤ (with the trivial involution) at
 * `σ = 1`. The ring arithmetic is supplied by that construction through [GaussianIntAlgebras].
 */
typealias GaussianInt = Complex<BigInteger>

/**
 * The distinguished Gaussian integers.
 */
object GaussianInts {
    val ZERO: GaussianInt = GaussianInt(BigInteger.ZERO, BigInteger.ZERO)
    val ONE: GaussianInt = GaussianInt(BigInteger.ONE, BigInteger.ZERO)
    val I: GaussianInt = GaussianInt(BigInteger.ZERO, BigInteger.ONE)
}
