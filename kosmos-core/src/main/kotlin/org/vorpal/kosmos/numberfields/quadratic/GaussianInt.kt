package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.algebra.structures.CD
import java.math.BigInteger

/**
 * A Gaussian integer `re + im·i ∈ ℤ[i]`.
 *
 * Algebraically `ℤ[i] = ℤ[x]/(x² + 1)` is the Cayley-Dickson doubling of ℤ (with the trivial
 * involution) at `σ = 1`; see [org.vorpal.kosmos.algebra.structures.CayleyDickson.commutative]. The
 * carrier is a [CD] pair, and the ring arithmetic is supplied by that construction through
 * [GaussianIntAlgebras].
 *
 * [GaussianInt] is a value class rather than a typealias of `CD<BigInteger>` so that it is a distinct
 * Kotlin type from the other algebras carried by `CD<BigInteger>` (the split-complex and dual
 * numbers over ℤ).
 */
@JvmInline
value class GaussianInt(val cd: CD<BigInteger>) {
    constructor(re: BigInteger, im: BigInteger) : this(CD(re, im))

    /** The real part. */
    val re: BigInteger
        get() = cd.a

    /** The imaginary part. */
    val im: BigInteger
        get() = cd.b

    operator fun component1(): BigInteger = re
    operator fun component2(): BigInteger = im

    override fun toString(): String =
        "GaussianInt(re=$re, im=$im)"

    companion object {
        val ZERO = GaussianInt(BigInteger.ZERO, BigInteger.ZERO)
        val ONE = GaussianInt(BigInteger.ONE, BigInteger.ZERO)
        val I = GaussianInt(BigInteger.ZERO, BigInteger.ONE)
    }
}
