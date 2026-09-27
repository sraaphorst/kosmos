package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat

object ArbRationalQuaternion {

    /**
     * Rationals `p/q` with `|p| ≤ 20` and `1 ≤ q ≤ 20`: small enough that images in floating point
     * (e.g. under `ℍ_ℚ → ℍ`) stay far inside the tolerance of approximate real equality.
     */
    private val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-20..20), Arb.int(1..20))
            .map { (p, q) -> Rational.of(p, q) }

    /**
     * Rational quaternions `ℍ_ℚ = CD⟨ℚ(i)⟩`, built from pairs of Gaussian rationals.
     */
    val rationalQuaternion: Arb<RationalQuaternion> =
        Arb.pair(ArbGaussianRat.small, ArbGaussianRat.small)
            .map { (a, b) -> CD(a, b) }

    /**
     * Rational quaternions with small coordinates; see [smallRational].
     */
    val smallRationalQuaternion: Arb<RationalQuaternion> =
        Arb.bind(smallRational, smallRational, smallRational, smallRational) { w, x, y, z ->
            rationalQuaternion(w, x, y, z)
        }

    /**
     * Nonzero rational quaternions: exactly the units of the division ring `ℍ_ℚ`.
     */
    val nonZeroRationalQuaternion: Arb<RationalQuaternion> =
        rationalQuaternion.filter { q ->
            q != RationalQuaternionAlgebras.RationalQuaternionDivisionRing.zero
        }
}
