package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw

/**
 * Confirms the *weak* associativity that [OctonionAlgebras.OctonionDivisionAlgebraReal] DOES satisfy,
 * complementing [OctonionNonAssociativitySpec] (which shows it is not fully associative).
 *
 * - [AlternativityLaw]: `x(xy) = (xx)y` and `(yx)x = y(xx)`. By Artin's theorem this is equivalent to
 *   any two elements generating an associative subalgebra — the precise sense in which 𝕆 is "almost"
 *   associative.
 * - [MoufangLaw]: the three Moufang identities, which are strictly stronger than alternativity and
 *   hold in every alternative algebra.
 *
 * These are bare [org.vorpal.kosmos.laws.TestingLaw]s rather than a bundled suite, so we invoke
 * `.test()` directly. We use the plain octonion generator (no reciprocal needed) and the approximate
 * octonion equality, since these identities are exact algebraically but evaluated in floating point.
 */
class OctonionAlternativitySpec : StringSpec({

    val ring = OctonionAlgebras.OctonionDivisionAlgebraReal
    val op = ring.mul.op
    val arb = ArbOctonion.octonion
    val eq = OctonionAlgebras.eqOctonion
    val pr = OctonionAlgebras.printableOctonionPretty

    "octonion multiplication is alternative: x(xy) = (xx)y and (yx)x = y(xx)" {
        AlternativityLaw(op, arb, eq, pr).test()
    }

    "octonion multiplication satisfies the Moufang identities" {
        MoufangLaw(op, arb, eq, pr).test()
    }
})
