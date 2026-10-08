package org.vorpal.kosmos.hypercomplex.bioctonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.hypercomplex.biquaternion.ArbBiquaternion
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras

object ArbBioctonion {

    /**
     * Bioctonions with small complex coefficients.
     */
    val bioctonion: Arb<Bioctonion> =
        Arb.list(ArbBiquaternion.smallComplex, 8..8).map(::bioctonion)

    /**
     * Units of `𝕆 ⊗ ℂ`, safely away from the null cone: the complex norm `N(o)` has squared modulus
     * in `[0.01, 10⁸]`.
     */
    val unitBioctonion: Arb<Bioctonion> =
        bioctonion.filter { o ->
            val n = BioctonionAlgebras.BioctonionAlgebra.normSq(o)
            ComplexAlgebras.ComplexStarAlgebra.normSq(n) in 0.01..1e8
        }
}
