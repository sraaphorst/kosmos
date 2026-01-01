package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw

/**
 * [EquivalenceRelationLaws]
 * - [ReflexivityLaw]
 * - [SymmetryLaw]
 * - [TransitivityLaw]
 */
class EquivalenceRelationLaws<A : Any>(
    relation: Relation<A>,
    arb: Arb<A>,
    pr: Printable<A>
): LawSuite {
    private val reflexivityLaw = ReflexivityLaw(relation, arb, pr)
    private val symmetryLaw = SymmetryLaw(relation, arb, pr)
    private val transitivityLaw = TransitivityLaw(relation, arb, pr)

    override fun laws(): List<TestingLaw> =
        listOf(reflexivityLaw, symmetryLaw, transitivityLaw)
}
