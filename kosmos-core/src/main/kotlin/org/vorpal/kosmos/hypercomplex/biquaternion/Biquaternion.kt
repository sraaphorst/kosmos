package org.vorpal.kosmos.hypercomplex.biquaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.complex.Complex

/**
 * A biquaternion (complex quaternion) `w + xi + yj + zk` with *complex* coefficients
 * `w, x, y, z ∈ ℂ`:
 * ```text
 * ℍ ⊗_ℝ ℂ,   i² = j² = k² = ijk = -1,
 * ```
 * where the complex scalars commute with `i, j, k`. To keep the two imaginary units apart, the
 * imaginary unit of the coefficient field ℂ is written `h` in this package's documentation, so the
 * eight real dimensions are spanned by `1, i, j, k, h, hi, hj, hk`.
 *
 * Algebraically this is the Hamilton quaternion algebra `(-1, -1)_ℂ`, built by the same two
 * Cayley-Dickson doublings as ℍ but starting from ℂ instead of ℝ:
 * ```text
 * Biquaternion = CD⟨ℂ[i]/(i² + 1)⟩ = CD⟨𝔹⟩,
 * ```
 * the doubling of the bicomplex numbers with their `j`-conjugation. It is associative and
 * noncommutative, and isomorphic to the matrix algebra `M₂(ℂ)`; see
 * [BiquaternionAlgebras.BiquaternionToComplexMatrixIsomorphism]. It is not a division algebra: its
 * complex-valued norm `w² + x² + y² + z²` vanishes on nonzero elements such as `1 + hi`.
 *
 * [Biquaternion] is a value class rather than a typealias of [CD] because its carrier
 * `CD<CD<Complex>> = CD<Quaternion>` is also the carrier of the octonions 𝕆.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [BiquaternionAlgebras].
 */
@JvmInline
value class Biquaternion(val cd: CD<CD<Complex>>) {

    /** The scalar part `w ∈ ℂ`. */
    val w: Complex get() = cd.a.a

    /** The complex coefficient of `i`. */
    val x: Complex get() = cd.a.b

    /** The complex coefficient of `j`. */
    val y: Complex get() = cd.b.a

    /** The complex coefficient of `k`. */
    val z: Complex get() = cd.b.b

    operator fun component1(): Complex = w
    operator fun component2(): Complex = x
    operator fun component3(): Complex = y
    operator fun component4(): Complex = z

    /**
     * The complex coefficients `[w, x, y, z]` of `w + xi + yj + zk`.
     */
    fun coefficients(): List<Complex> =
        listOf(w, x, y, z)

    override fun toString(): String =
        "Biquaternion(w=$w, x=$x, y=$y, z=$z)"
}

fun biquaternion(
    w: Complex,
    x: Complex,
    y: Complex,
    z: Complex
): Biquaternion =
    Biquaternion(CD(CD(w, x), CD(y, z)))
