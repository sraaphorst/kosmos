package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * A (Hamilton) quaternion `w + xi + yj + zk` over a base ring [R], with
 * ```text
 * i² = j² = k² = ijk = -1,   ij = k = -ji.
 * ```
 *
 * Algebraically this is the Cayley-Dickson doubling of the complex-like ring `R[i]/(i² + 1)` at
 * `σ = 1`:
 * ```text
 * Quaternion<R> = CD⟨CD⟨R⟩⟩,   (a, b) = a + bj,   a = w + xi,   b = y + zi,
 * ```
 * i.e. the quaternion algebra `(-1, -1)_R`. The three classical coefficient rings have names:
 * - [RealQuaternion] `= Quaternion<Real>`: the division algebra ℍ.
 * - [RationalQuaternion] `= Quaternion<Rational>`: the division algebra `ℍ_ℚ`.
 * - [LipschitzQuaternion] `= Quaternion<BigInteger>`: the Lipschitz order `ℤ⟨i, j, k⟩`.
 *
 * [Quaternion] is a value class rather than a typealias of [CD] because its carrier `CD<CD<R>>` is
 * shared: over ℝ it is `CD<Complex>`, also the carrier of the split-quaternions and (inside
 * `Bicomplex`) of the bicomplex numbers. The algebras share a carrier but not a multiplication.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [QuaternionAlgebras] (generic, and over ℝ), [RationalQuaternionAlgebras] and
 * [LipschitzQuaternionAlgebras].
 */
@JvmInline
value class Quaternion<R : Any>(val cd: CD<CD<R>>) {
    constructor(w: R, x: R, y: R, z: R) : this(CD(CD(w, x), CD(y, z)))

    /** The scalar part `w`. */
    val w: R
        get() = cd.a.a

    /** The coefficient of `i`. */
    val x: R
        get() = cd.a.b

    /** The coefficient of `j`. */
    val y: R
        get() = cd.b.a

    /** The coefficient of `k`. */
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
        "Quaternion(w=$w, x=$x, y=$y, z=$z)"
}

/** The real quaternions ℍ. */
typealias RealQuaternion = Quaternion<Real>

/** The rational quaternions `ℍ_ℚ`. */
typealias RationalQuaternion = Quaternion<Rational>

/** The Lipschitz quaternions `ℤ⟨i, j, k⟩`: quaternions with integer coefficients. */
typealias LipschitzQuaternion = Quaternion<BigInteger>

/**
 * The quaternion `w + xi + yj + zk`.
 */
fun <R : Any> quaternion(
    w: R,
    x: R,
    y: R,
    z: R
): Quaternion<R> =
    Quaternion(w, x, y, z)
