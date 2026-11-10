package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNodes

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
) : StructuredLawSuite {
    private val reflexivityLaw = ReflexivityLaw(relation, arb, pr)
    private val symmetryLaw = SymmetryLaw(relation, arb, pr)
    private val transitivityLaw = TransitivityLaw(relation, arb, pr)

    override val binding = LawBinding.of(this, relation, arb, pr)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(reflexivityLaw, symmetryLaw, transitivityLaw).asNodes()
}
