package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Confirms the *weak* associativity that [OctonionAlgebras.OctonionDivisionAlgebra] DOES satisfy,
 * complementing [OctonionNonAssociativitySpec] (which shows it is not fully associative).
 *
 * - [AlternativityLaw]: `x(xy) = (xx)y` and `(yx)x = y(xx)`. By Artin's theorem this is equivalent to
 *   any two elements generating an associative subalgebra — the precise sense in which 𝕆 is "almost"
 *   associative.
 * - [MoufangLaw]: the three Moufang identities, which are strictly stronger than alternativity and
 *   hold in every alternative algebra.
 *
 * Both are small suites of their one-sided halves, registered on their own rather than as part of a
 * structure suite. We use the plain octonion generator (no reciprocal needed) and the approximate
 * octonion equality, since these identities are exact algebraically but evaluated in floating point.
 */
class OctonionAlternativitySpec : FunSpec({
    lawTests(lawGraph("𝕆 is alternative") {
        val ring = OctonionAlgebras.OctonionDivisionAlgebra
        val op = ring.mul.op
        val arb = ArbOctonion.octonion
        val eq = OctonionAlgebras.eqOctonion
        val pr = OctonionAlgebras.printableOctonionPretty

        branch("octonion multiplication is alternative: x(xy) = (xx)y and (yx)x = y(xx)") {
            suite(AlternativityLaw(op, arb, eq, pr))
        }

        branch("octonion multiplication satisfies the Moufang identities") {
            suite(MoufangLaw(op, arb, eq, pr))
        }
    })
})
