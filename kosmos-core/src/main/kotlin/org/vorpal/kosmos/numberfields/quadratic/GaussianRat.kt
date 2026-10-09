package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.rational.Rational

/**
 * A Gaussian rational `re + im·i ∈ ℚ(i)`.
 *
 * Algebraically `ℚ(i) = ℚ[x]/(x² + 1)` is the Cayley-Dickson doubling of ℚ (with the trivial
 * involution) at `σ = 1`; see [org.vorpal.kosmos.algebra.structures.CayleyDickson.commutative]. The
 * carrier is a [CD] pair, and the ring arithmetic is supplied by that construction through
 * [GaussianRatAlgebras].
 *
 * [GaussianRat] is a value class rather than a typealias of `CD<Rational>` so that it is a distinct
 * Kotlin type from the other algebras carried by `CD<Rational>` (the split-complex and dual numbers
 * over ℚ).
 */
@JvmInline
value class GaussianRat(val cd: CD<Rational>) {
    constructor(re: Rational, im: Rational) : this(CD(re, im))

    /** The real part. */
    val re: Rational
        get() = cd.a

    /** The imaginary part. */
    val im: Rational
        get() = cd.b

    operator fun component1(): Rational = re
    operator fun component2(): Rational = im

    override fun toString(): String =
        "GaussianRat(re=$re, im=$im)"

    companion object {
        val ZERO = GaussianRat(Rational.ZERO, Rational.ZERO)
        val ONE = GaussianRat(Rational.ONE, Rational.ZERO)
        val I = GaussianRat(Rational.ZERO, Rational.ONE)
    }
}
