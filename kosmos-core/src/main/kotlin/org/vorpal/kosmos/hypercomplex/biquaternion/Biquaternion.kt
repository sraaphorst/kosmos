package org.vorpal.kosmos.hypercomplex.biquaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * A biquaternion over a commutative base ring [R].
 * ```text
 * Biquaternion(R) = Quaternion(R) ⊗_R Complex(R) ≅ Quaternion(Complex(R)).
 * ```
 * Elements have the form
 * ```text
 * w + xi + yj + zk,   w, x, y, z ∈ Complex(R),   i² = j² = k² = ijk = -1,
 * ```
 * and the complex imaginary unit commutes with the quaternion units `i`, `j` and `k`. To keep the
 * two kinds of imaginary unit apart, the unit of the coefficient ring `Complex(R)` is written `h` in
 * this package's documentation, so a basis over `R` is `1, i, j, k, h, hi, hj, hk`.
 *
 * This is an associative, generally noncommutative algebra of rank 8 over `R`. It is the
 * Cayley-Dickson doubling of `Complex(R)[i]/(i² + 1) = Bicomplex(R)`, built by the same two
 * doublings as the quaternions but starting from `Complex(R)` instead of `R`.
 *
 * The classical case has a name: [RealBiquaternion] `= Biquaternion<Real>`, the complex quaternions
 * `ℍ ⊗_ℝ ℂ ≅ M₂(ℂ)`. It is not a division algebra: its complex-valued norm `w² + x² + y² + z²`
 * vanishes on nonzero elements such as `1 + hi`.
 *
 * [Biquaternion] is a value class rather than a typealias of [CD] so that it is a distinct Kotlin
 * type from the other algebras carried by nested Cayley-Dickson pairs.
 *
 * Algebraic operations are supplied by [BiquaternionAlgebras].
 */
@JvmInline
value class Biquaternion<R : Any>(val cd: CD<CD<Complex<R>>>) {

    /** The scalar coefficient `w ∈ Complex(R)`. */
    val w: Complex<R> get() = cd.a.a

    /** The coefficient of `i`. */
    val x: Complex<R> get() = cd.a.b

    /** The coefficient of `j`. */
    val y: Complex<R> get() = cd.b.a

    /** The coefficient of `k`. */
    val z: Complex<R> get() = cd.b.b

    operator fun component1(): Complex<R> = w
    operator fun component2(): Complex<R> = x
    operator fun component3(): Complex<R> = y
    operator fun component4(): Complex<R> = z

    /**
     * The four complex coefficients `[w, x, y, z]`.
     */
    fun coefficients(): List<Complex<R>> =
        listOf(w, x, y, z)

    /**
     * The eight base-ring coefficients, in the order
     * `[Re w, Im w, Re x, Im x, Re y, Im y, Re z, Im z]`.
     */
    fun baseCoefficients(): List<R> =
        coefficients().flatMap { c -> listOf(c.re, c.im) }

    override fun toString(): String =
        "Biquaternion(w=$w, x=$x, y=$y, z=$z)"
}

/** The complex quaternions `ℍ ⊗_ℝ ℂ`. */
typealias RealBiquaternion = Biquaternion<Real>

/**
 * The biquaternion `w + xi + yj + zk`.
 */
fun <R : Any> biquaternion(
    w: Complex<R>,
    x: Complex<R>,
    y: Complex<R>,
    z: Complex<R>,
): Biquaternion<R> =
    Biquaternion(CD(CD(w, x), CD(y, z)))
