package org.vorpal.kosmos.hypercomplex.biquaternion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.hypercomplex.complex.RealComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras

object ArbBiquaternion {

    /**
     * Complex numbers with small real and imaginary parts.
     */
    val smallComplex: Arb<RealComplex> =
        Arb.pair(ArbReal.smallReal, ArbReal.smallReal).map { (re, im) -> complex(re, im) }

    /**
     * Biquaternions with small complex coefficients.
     */
    val biquaternion: Arb<Biquaternion> =
        Arb.bind(smallComplex, smallComplex, smallComplex, smallComplex) { w, x, y, z ->
            biquaternion(w, x, y, z)
        }

    /**
     * Units of `ℍ ⊗ ℂ`, safely away from the null cone: the complex norm `N(q)` has squared modulus
     * in `[0.01, 10⁸]`, so `q⁻¹` is numerically well-conditioned.
     */
    val unitBiquaternion: Arb<Biquaternion> =
        biquaternion.filter { q ->
            val n = BiquaternionAlgebras.BiquaternionRing.normSq(q)
            ComplexAlgebras.ComplexStarAlgebra.normSq(n) in 0.01..1e8
        }
}
