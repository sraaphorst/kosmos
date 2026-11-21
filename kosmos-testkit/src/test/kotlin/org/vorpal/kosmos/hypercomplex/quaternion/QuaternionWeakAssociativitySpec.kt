package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.BolIdentityLaw
import org.vorpal.kosmos.laws.property.FlexibilityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.testing.laws.lawTests

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
class QuaternionWeakAssociativitySpec : FunSpec({
    lawTests(lawGraph("ℍ weak associativity") {
        val op = QuaternionAlgebras.QuaternionDivisionRing.mul.op
        val arb = ArbQuaternion.quaternion
        val eq = QuaternionAlgebras.eqQuaternion
        val pr = QuaternionAlgebras.printableQuaternionPretty

        branch("quaternion multiplication is alternative") {
            suite(AlternativityLaw(op, arb, eq, pr))
        }

        branch("quaternion multiplication satisfies the Moufang identities") {
            suite(MoufangLaw(op, arb, eq, pr))
        }

        branch("quaternion multiplication satisfies both Bol identities") {
            suite(BolIdentityLaw(op, arb, eq, pr))
        }

        law("quaternion multiplication is flexible", FlexibilityLaw(op, arb, eq, pr))
    })
})
