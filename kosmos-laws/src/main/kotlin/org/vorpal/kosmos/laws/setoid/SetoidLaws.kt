package org.vorpal.kosmos.laws.setoid

import io.kotest.property.Arb
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.setoid.Setoid
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.relation.EquivalenceRelationLaws

/**
 * [org.vorpal.kosmos.core.setoid.Setoid] laws:
 * - The `Eq` on the Setoid is an equivalence relation:
 * - [EquivalenceRelationLaws]
 */
class SetoidLaws<A : Any>(
    private val setoid: Setoid<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override fun laws(): List<TestingLaw> =
        EquivalenceRelationLaws(setoid.relation, arb, pr).laws()
}
