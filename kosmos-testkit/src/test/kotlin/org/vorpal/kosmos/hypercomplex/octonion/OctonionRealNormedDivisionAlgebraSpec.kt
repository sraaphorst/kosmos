package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.RealNormedDivisionAlgebraLaws

/**
 * Property tests for [OctonionAlgebras.OctonionDivisionAlgebraReal] as a
 * [org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra].
 *
 * This is the real "does Cayley-Dickson on the quaternions actually give the octonions" check:
 * [RealNormedDivisionAlgebraLaws] bundles the non-associative involutive ring laws, multiplicative
 * invertibility (so 𝕆 is genuinely a division algebra), multiplicativity of the squared norm
 * `N(xy) = N(x)N(y)` (the composition-algebra property), positive-definiteness of `N`, and the
 * `‖a‖² = max(0, N(a))` consistency relation.
 *
 * We draw from [ArbOctonion.reciprocalSafeOctonion] so that the invertibility law never hits the
 * `normSq ≈ 0` guard in the reciprocal, and the multiplicative norm law stays numerically well-conditioned.
 */
object OctonionRealNormedDivisionAlgebraSpec : StringSpec({
    "OctonionDivisionAlgebraReal satisfies RealNormedDivisionAlgebraLaws" {
        RealNormedDivisionAlgebraLaws(
            algebra = OctonionAlgebras.OctonionDivisionAlgebraReal,
            arb = ArbOctonion.reciprocalSafeOctonion,
            eqA = OctonionAlgebras.eqOctonion,
            eqReal = RealAlgebras.eqRealApprox,
            prA = OctonionAlgebras.printableOctonionPretty,
            prReal = RealAlgebras.printableRealPretty
        ).fullTest().throwIfFailed()
    }
})
