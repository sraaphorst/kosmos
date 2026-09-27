package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.quaternion.ArbLipschitzQuaternion

object ArbGravesianOctonion {

    /**
     * Gravesian octonions `CD⟨ℤ⟨i, j, k⟩⟩ ≅ ℤ⁸`, built from pairs of Lipschitz quaternions.
     */
    val gravesianOctonion: Arb<GravesianOctonion> =
        Arb.pair(ArbLipschitzQuaternion.lipschitzQuaternion, ArbLipschitzQuaternion.lipschitzQuaternion)
            .map { (a, b) -> CD(a, b) }
}
