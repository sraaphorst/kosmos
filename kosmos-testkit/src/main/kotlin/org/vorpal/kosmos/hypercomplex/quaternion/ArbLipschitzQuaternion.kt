package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianInt

object ArbLipschitzQuaternion {

    /**
     * Lipschitz quaternions `ℤ⟨i, j, k⟩`, built from pairs of Gaussian integers `(w + xi, y + zi)`.
     */
    val lipschitzQuaternion: Arb<LipschitzQuaternion> =
        Arb.pair(ArbGaussianInt.small, ArbGaussianInt.small)
            .map { (a, b) -> Quaternion(CD(a.cd, b.cd)) }

    /**
     * The 8 units `±1, ±i, ±j, ±k` of the Lipschitz order.
     */
    val lipschitzUnit: Arb<LipschitzQuaternion> =
        Arb.element(LipschitzQuaternionAlgebras.LipschitzQuaternionRing.units)
}
