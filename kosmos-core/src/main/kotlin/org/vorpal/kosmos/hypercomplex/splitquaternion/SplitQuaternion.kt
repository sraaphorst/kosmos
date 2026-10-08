package org.vorpal.kosmos.hypercomplex.splitquaternion

import org.vorpal.kosmos.algebra.structures.CD

/**
 * A split-quaternion (coquaternion) `w + xi + yj + zk` over a base ring [R], with
 * ```text
 * i² = -1,   j² = +1,   k² = +1,   ij = k = -ji.
 * ```
 *
 * Algebraically this is the Cayley-Dickson doubling of the complex-like ring `R[i]/(i² + 1)` at
 * `σ = -1`:
 * ```text
 * SplitQuaternion<R> = CD_split⟨CD⟨R⟩⟩,   (a, b) = a + bj,   a = w + xi,   b = y + zi,
 * ```
 * i.e. the generalized quaternion algebra `(-1, +1)_R`. It is associative and noncommutative, and
 * (when 2 is invertible in `R`) isomorphic to the matrix ring `M₂(R)`; see
 * [SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism].
 *
 * Unlike the quaternions it is not a division ring, even over ℝ. Its norm
 * ```text
 * N(w + xi + yj + zk) = w² + x² - y² - z²
 * ```
 * is isotropic, and the elements with `N = 0` are zero divisors, e.g. `(1 + j)(1 - j) = 0`.
 *
 * [SplitQuaternion] is a value class rather than a typealias of [CD] because over ℝ its carrier
 * `CD<CD<Real>> = CD<Complex>` is also the carrier of the quaternions ℍ and (inside `Bicomplex`) of
 * the bicomplex numbers 𝔹. The three algebras share a carrier but not a multiplication.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [SplitQuaternionAlgebras].
 */
@JvmInline
value class SplitQuaternion<R : Any>(val cd: CD<CD<R>>) {
    constructor(w: R, x: R, y: R, z: R) : this(CD(CD(w, x), CD(y, z)))

    /** The scalar part `w`. */
    val w: R
        get() = cd.a.a

    /** The coefficient of `i` (`i² = -1`). */
    val x: R
        get() = cd.a.b

    /** The coefficient of `j` (`j² = +1`). */
    val y: R
        get() = cd.b.a

    /** The coefficient of `k` (`k² = +1`). */
    val z: R
        get() = cd.b.b

    operator fun component1(): R = w
    operator fun component2(): R = x
    operator fun component3(): R = y
    operator fun component4(): R = z

    /**
     * The coefficients `[w, x, y, z]` of `w + xi + yj + zk`.
     */
    fun coefficients(): List<R> =
        listOf(w, x, y, z)

    override fun toString(): String =
        "SplitQuaternion(w=$w, x=$x, y=$y, z=$z)"
}

fun <R : Any> splitQuaternion(
    w: R,
    x: R,
    y: R,
    z: R
): SplitQuaternion<R> =
    SplitQuaternion(w, x, y, z)
