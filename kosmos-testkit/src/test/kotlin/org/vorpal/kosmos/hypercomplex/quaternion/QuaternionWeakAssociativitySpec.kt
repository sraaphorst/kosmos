package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.BolIdentityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw

/**
 * The quaternions are associative, so they satisfy every "weak associativity" identity in the loop
 * hierarchy as an immediate consequence: alternativity, the Moufang identities, both Bol identities,
 * and flexibility. (Associative ⟹ Moufang ⟹ {left,right} Bol and alternative ⟹ flexible.)
 *
 * Running these against ℍ therefore serves two purposes:
 *  1. It positively confirms ℍ sits at the top of the hierarchy (a sanity counterpart to
 *     `QuaternionStarAlgebraSpec`'s direct associativity check).
 *  2. It exercises the law implementations themselves on a structure where every identity *must* hold,
 *     acting as a regression guard — in particular for [MoufangLaw], whose middle identity was recently
 *     corrected, and for the [BolIdentityLaw]/[FlexibilityLaw] formulas.
 */
class QuaternionWeakAssociativitySpec : StringSpec({

    val op = QuaternionAlgebras.QuaternionDivisionRing.mul.op
    val arb = ArbQuaternion.quaternion
    val eq = QuaternionAlgebras.eqQuaternion
    val pr = QuaternionAlgebras.printableQuaternionPretty

    "quaternion multiplication is alternative" {
        AlternativityLaw(op, arb, eq, pr).test()
    }

    "quaternion multiplication satisfies the Moufang identities" {
        MoufangLaw(op, arb, eq, pr).test()
    }

    "quaternion multiplication satisfies both Bol identities" {
        BolIdentityLaw(op, arb, eq, pr).test()
    }

    "quaternion multiplication is flexible" {
        FlexibilityLaw(op, arb, eq, pr).test()
    }
})
