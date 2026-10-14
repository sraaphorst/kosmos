package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real

/**
 * A complex-like number `re + im·i` over a base ring [R], with `i² = -1`.
 *
 * Algebraically this is the commutative quadratic extension
 * ```text
 * Complex<R> = R[i]/(i² + 1),
 * ```
 * the Cayley-Dickson doubling of `R` (with the trivial involution) at `σ = 1`; see
 * [org.vorpal.kosmos.algebra.structures.CayleyDickson.commutative]. The classical coefficient rings
 * have names:
 * - [RealComplex] `= Complex<Real>`: the field ℂ.
 * - `GaussianRat = Complex<Rational>`: the field `ℚ(i)`.
 * - `GaussianInt = Complex<BigInteger>`: the Euclidean domain `ℤ[i]`.
 *
 * [Complex] is a value class rather than a typealias of [CD] because its carrier `CD<R>` is shared
 * with the split-complex numbers (`i² = +1`) and the dual numbers (`i² = 0`) over the same `R`. The
 * algebras share a carrier but not a multiplication.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [ComplexAlgebras] (generic, and over ℝ) and, for the Gaussian rings, by
 * `GaussianIntAlgebras` and `GaussianRatAlgebras`.
 */
@JvmInline
value class Complex<R : Any>(val cd: CD<R>) {
    constructor(re: R, im: R) : this(CD(re, im))

    /** The real part. */
    val re: R
        get() = cd.a

    /** The imaginary part. */
    val im: R
        get() = cd.b

    operator fun component1(): R = re
    operator fun component2(): R = im

    override fun toString(): String =
        "Complex(re=$re, im=$im)"
}

/** The complex numbers ℂ. */
typealias RealComplex = Complex<Real>

/**
 * The complex-like number `re + im·i`.
 */
fun <R : Any> complex(re: R, im: R): Complex<R> =
    Complex(re, im)
