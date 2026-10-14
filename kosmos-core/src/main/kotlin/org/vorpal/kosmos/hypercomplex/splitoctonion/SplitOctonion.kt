package org.vorpal.kosmos.hypercomplex.splitoctonion

import org.vorpal.kosmos.algebra.structures.CD

/**
 * A split-octonion over a base ring [R]:
 * ```text
 * w + x·e₁ + y·e₂ + z·e₃ + u·e₄ + v·e₅ + s·e₆ + t·e₇,
 * ```
 * where `e₁, e₂, e₃` square to `-1` and `e₄, e₅, e₆, e₇` square to `+1`.
 *
 * Algebraically this is the Cayley-Dickson doubling of the Hamilton quaternions over `R` at
 * `σ = -1`:
 * ```text
 * SplitOctonion<R> = CD_split⟨ℍ_R⟩,   (a, b) = a + bℓ,   ℓ = e₄,   ℓ² = +1,
 * ```
 * with `a = w + xi + yj + zk` and `b = u + vi + sj + tk`. It is nonassociative but alternative, and
 * it is a composition algebra: its norm
 * ```text
 * N = w² + x² + y² + z² - u² - v² - s² - t²
 * ```
 * is multiplicative. The norm has signature `(4, 4)`, so unlike the octonions the split-octonions
 * have zero divisors, e.g. `(1 + e₄)(1 - e₄) = 0`.
 *
 * [SplitOctonion] is a value class rather than a typealias of [CD] because over ℝ its carrier
 * `CD<CD<CD<Real>>> = CD<Quaternion>` is also the carrier of the octonions 𝕆.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [SplitOctonionAlgebras].
 */
@JvmInline
value class SplitOctonion<R : Any>(val cd: CD<CD<CD<R>>>) {

    /** The scalar part. */
    val w: R get() = cd.a.a.a
    /** The coefficient of `e₁` (`e₁² = -1`). */
    val x: R get() = cd.a.a.b
    /** The coefficient of `e₂` (`e₂² = -1`). */
    val y: R get() = cd.a.b.a
    /** The coefficient of `e₃` (`e₃² = -1`). */
    val z: R get() = cd.a.b.b

    /** The coefficient of `e₄` (`e₄² = +1`). */
    val u: R get() = cd.b.a.a
    /** The coefficient of `e₅` (`e₅² = +1`). */
    val v: R get() = cd.b.a.b
    /** The coefficient of `e₆` (`e₆² = +1`). */
    val s: R get() = cd.b.b.a
    /** The coefficient of `e₇` (`e₇² = +1`). */
    val t: R get() = cd.b.b.b

    /**
     * The coefficients `[w, x, y, z, u, v, s, t]` in the basis `1, e₁, …, e₇`.
     */
    fun coefficients(): List<R> =
        listOf(w, x, y, z, u, v, s, t)

    override fun toString(): String =
        "SplitOctonion(${coefficients().joinToString(", ")})"
}

fun <R : Any> splitOctonion(
    w: R, x: R, y: R, z: R,
    u: R, v: R, s: R, t: R
): SplitOctonion<R> =
    SplitOctonion(CD(CD(CD(w, x), CD(y, z)), CD(CD(u, v), CD(s, t))))
