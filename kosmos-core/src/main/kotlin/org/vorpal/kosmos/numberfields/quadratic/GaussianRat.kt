package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * A Gaussian rational `re + im·i ∈ ℚ(i)`: a [Complex] number with rational coefficients.
 *
 * `ℚ(i) = ℚ[x]/(x² + 1)` is the Cayley-Dickson doubling of ℚ (with the trivial involution) at
 * `σ = 1`. The ring arithmetic is supplied by that construction through [GaussianRatAlgebras].
 */
typealias GaussianRat = Complex<Rational>

/**
 * The distinguished Gaussian rationals.
 */
object GaussianRats {
    val ZERO: GaussianRat = GaussianRat(Rational.ZERO, Rational.ZERO)
    val ONE: GaussianRat = GaussianRat(Rational.ONE, Rational.ZERO)
    val I: GaussianRat = GaussianRat(Rational.ZERO, Rational.ONE)
}
