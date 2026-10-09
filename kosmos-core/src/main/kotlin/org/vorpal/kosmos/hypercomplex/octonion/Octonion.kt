package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * An octonion over a base ring [R]:
 * ```text
 * w + x·e₁ + y·e₂ + z·e₃ + u·e₄ + v·e₅ + s·e₆ + t·e₇,
 * ```
 * where all seven imaginary units square to `-1`.
 *
 * Algebraically this is the Cayley-Dickson doubling of the quaternions over `R` at `σ = 1`:
 * ```text
 * Octonion<R> = CD⟨Quaternion<R>⟩,   (a, b) = a + bℓ,   ℓ = e₄,   ℓ² = -1,
 * ```
 * with `a = w + xi + yj + zk` and `b = u + vi + sj + tk`. It is nonassociative but alternative. The
 * three classical coefficient rings have names:
 * - [RealOctonion] `= Octonion<Real>`: the division algebra 𝕆.
 * - [RationalOctonion] `= Octonion<Rational>`: the division algebra `𝕆_ℚ`.
 * - [GravesianOctonion] `= Octonion<BigInteger>`: the Gravesian integers `ℤ⁸ ⊂ 𝕆_ℚ`.
 *
 * [Octonion] is a value class rather than a typealias of [CD] because its carrier `CD<CD<CD<R>>>` is
 * shared with the split-octonions and, over ℝ, with the biquaternions.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [OctonionAlgebras] (generic, and over ℝ), [RationalOctonionAlgebras] and
 * [GravesianOctonionAlgebras].
 */
@JvmInline
value class Octonion<R : Any>(val cd: CD<CD<CD<R>>>) {

    /** The scalar part. */
    val w: R get() = cd.a.a.a
    /** The coefficient of `e₁`. */
    val x: R get() = cd.a.a.b
    /** The coefficient of `e₂`. */
    val y: R get() = cd.a.b.a
    /** The coefficient of `e₃`. */
    val z: R get() = cd.a.b.b

    /** The coefficient of `e₄`. */
    val u: R get() = cd.b.a.a
    /** The coefficient of `e₅`. */
    val v: R get() = cd.b.a.b
    /** The coefficient of `e₆`. */
    val s: R get() = cd.b.b.a
    /** The coefficient of `e₇`. */
    val t: R get() = cd.b.b.b

    /**
     * The coefficients `[w, x, y, z, u, v, s, t]` in the basis `1, e₁, …, e₇`.
     */
    fun coefficients(): List<R> =
        listOf(w, x, y, z, u, v, s, t)

    override fun toString(): String =
        "Octonion(${coefficients().joinToString(", ")})"
}

/** The real octonions 𝕆. */
typealias RealOctonion = Octonion<Real>

/** The rational octonions `𝕆_ℚ`. */
typealias RationalOctonion = Octonion<Rational>

/**
 * The Gravesian octonions: octonions with integer coefficients in the standard Cayley-Dickson
 * basis, i.e. the doubling of the Lipschitz quaternions.
 *
 * Additively they form the standard lattice `ℤ⁸`; multiplicatively they are closed under the
 * nonassociative octonion product. They should not be confused with the Cayley integers
 * (octavians), a maximal order whose lattice is `E₈`.
 */
typealias GravesianOctonion = Octonion<BigInteger>

/**
 * The octonion `w + x·e₁ + y·e₂ + z·e₃ + u·e₄ + v·e₅ + s·e₆ + t·e₇`.
 */
fun <R : Any> octonion(
    w: R, x: R, y: R, z: R,
    u: R, v: R, s: R, t: R
): Octonion<R> =
    Octonion(CD(CD(CD(w, x), CD(y, z)), CD(CD(u, v), CD(s, t))))
