package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.RealNormedDivisionAlgebraLaws

/**
 * Property tests for [QuaternionAlgebras.QuaternionDivisionRing] as a
 * [org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra].
 *
 * This is the quaternion analogue of `OctonionRealNormedDivisionAlgebraSpec`: it exercises the
 * non-associative involutive ring laws (which ℍ of course also satisfies), multiplicative
 * invertibility (ℍ is a division ring), multiplicativity of the squared norm `N(xy) = N(x)N(y)`
 * (the composition-algebra property), positive-definiteness of `N`, and the `‖a‖² = max(0, N(a))`
 * consistency relation.
 *
 * We draw from [ArbQuaternion.reciprocalSafeQuaternion] so the invertibility law never hits the
 * `normSq ≈ 0` guard in the reciprocal and the multiplicative-norm law stays well-conditioned.
 */
object QuaternionRealNormedDivisionAlgebraSpec : StringSpec({
    "QuaternionDivisionRing satisfies RealNormedDivisionAlgebraLaws" {
        RealNormedDivisionAlgebraLaws(
            algebra = QuaternionAlgebras.QuaternionDivisionRing,
            arb = ArbQuaternion.reciprocalSafeQuaternion,
            eqA = QuaternionAlgebras.eqQuaternion,
            eqReal = RealAlgebras.eqRealApprox,
            prA = QuaternionAlgebras.printableQuaternionPretty,
            prReal = RealAlgebras.printableRealPretty
        ).fullTest().throwIfFailed()
    }
})
