package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * An element of the Hurwitz order
 * ```text
 * 𝓗 = { a + bi + cj + dk : a, b, c, d ∈ ℤ  or  a, b, c, d ∈ ℤ + 1/2 } ⊂ ℍ_ℚ.
 * ```
 *
 * A Hurwitz quaternion *is* a rational quaternion: it is not another quaternion implementation, but
 * an element of a distinguished subring of [RationalQuaternion]. Accordingly:
 * - It is represented by its underlying [quaternion] in `ℍ_ℚ`.
 * - It contributes no quaternion arithmetic of its own. The ring structure of `𝓗`
 *   ([HurwitzQuaternionAlgebras.HurwitzQuaternionRing]) is the restriction of the
 *   Cayley-Dickson-built [RationalQuaternionAlgebras.RationalQuaternionDivisionRing] to `𝓗`,
 *   which is closed under `+`, `-`, `·` and conjugation.
 * - Operations under which `𝓗` is *not* closed stay in the ambient algebra. For example,
 *   `2 ∈ 𝓗` but `2⁻¹ = 1/2 ∉ 𝓗`: division is performed in `ℍ_ℚ` after the inclusion
 *   [HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism].
 *
 * Invariant: for every `h: HurwitzQuaternion`, `h.quaternion ∈ 𝓗`.
 *
 * The constructor is private, so there is no public route to an invalid instance: external
 * construction goes through the checked [Companion.invoke] overloads, while the ring operations,
 * which provably preserve `𝓗`, use the trusted internal [Companion.wrap].
 *
 * This set is the unique maximal order of the rational quaternions containing the Lipschitz order.
 */
@JvmInline
value class HurwitzQuaternion private constructor(
    val quaternion: RationalQuaternion
) {
    override fun toString(): String =
        "HurwitzQuaternion(w=$w, x=$x, y=$y, z=$z)"

    companion object {
        /**
         * Checked construction from a rational quaternion.
         *
         * @throws IllegalArgumentException if [q] is not in the Hurwitz order.
         */
        operator fun invoke(q: RationalQuaternion): HurwitzQuaternion {
            require(isHurwitz(q)) {
                "Not a Hurwitz quaternion: coordinates must be all integers or all half-integers: $q"
            }
            return HurwitzQuaternion(q)
        }

        /**
         * Checked construction from coordinates `a + bi + cj + dk`.
         */
        operator fun invoke(
            a: Rational,
            b: Rational,
            c: Rational,
            d: Rational
        ): HurwitzQuaternion =
            invoke(rationalQuaternion(a, b, c, d))

        /**
         * Trusted wrapping for values already known to lie in `𝓗`, e.g. results of the ring
         * operations of [HurwitzQuaternionAlgebras.HurwitzQuaternionRing] on Hurwitz inputs,
         * or images of Lipschitz quaternions.
         *
         * The membership invariant is *not* re-checked: it has been established once at the
         * boundary and is preserved by the algebra.
         */
        internal fun wrap(q: RationalQuaternion): HurwitzQuaternion =
            HurwitzQuaternion(q)

        /**
         * A rational quaternion `(a, b, c, d)` is Hurwitz iff:
         * - `2a`, `2b`, `2c`, `2d` are all integers; and
         * - `2a ≡ 2b ≡ 2c ≡ 2d (mod 2)`.
         */
        fun isHurwitz(q: RationalQuaternion): Boolean {
            val doubled = listOf(q.w, q.x, q.y, q.z).map { it * BigInteger.TWO }
            return doubled.all(Rational::isInteger) &&
                doubled.map { it.numerator.mod(BigInteger.TWO) }.distinct().size == 1
        }

        fun isHurwitz(a: Rational, b: Rational, c: Rational, d: Rational): Boolean =
            isHurwitz(rationalQuaternion(a, b, c, d))

        private val HALF: Rational = Rational.of(1, 2)

        val ZERO: HurwitzQuaternion = wrap(rationalQuaternion(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO))
        val ONE: HurwitzQuaternion = wrap(rationalQuaternion(Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ZERO))
        val I: HurwitzQuaternion = wrap(rationalQuaternion(Rational.ZERO, Rational.ONE, Rational.ZERO, Rational.ZERO))
        val J: HurwitzQuaternion = wrap(rationalQuaternion(Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.ZERO))
        val K: HurwitzQuaternion = wrap(rationalQuaternion(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ONE))

        /**
         * The Hurwitz unit `ω = (1 + i + j + k)/2`, which together with `1, i, j` generates `𝓗`
         * as a `ℤ`-module.
         */
        val OMEGA: HurwitzQuaternion = wrap(rationalQuaternion(HALF, HALF, HALF, HALF))
    }
}

val HurwitzQuaternion.w: Rational get() = quaternion.w
val HurwitzQuaternion.x: Rational get() = quaternion.x
val HurwitzQuaternion.y: Rational get() = quaternion.y
val HurwitzQuaternion.z: Rational get() = quaternion.z
