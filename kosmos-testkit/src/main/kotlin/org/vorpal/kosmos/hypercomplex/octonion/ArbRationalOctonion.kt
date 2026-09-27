package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion

object ArbRationalOctonion {

    /**
     * Rational octonions `𝕆_ℚ = CD⟨ℍ_ℚ⟩`, built from pairs of rational quaternions.
     */
    val rationalOctonion: Arb<RationalOctonion> =
        Arb.pair(ArbRationalQuaternion.rationalQuaternion, ArbRationalQuaternion.rationalQuaternion)
            .map { (a, b) -> CD(a, b) }

    /**
     * Rational octonions with small numerators and denominators, keeping exact arithmetic in the
     * longer identities (Moufang, norm multiplicativity) fast.
     */
    val smallRationalOctonion: Arb<RationalOctonion> =
        Arb.pair(ArbRationalQuaternion.smallRationalQuaternion, ArbRationalQuaternion.smallRationalQuaternion)
            .map { (a, b) -> CD(a, b) }

    val nonZeroRationalOctonion: Arb<RationalOctonion> =
        smallRationalOctonion.filter { o ->
            o != RationalOctonionAlgebras.RationalOctonionDivisionAlgebra.zero
        }
}
