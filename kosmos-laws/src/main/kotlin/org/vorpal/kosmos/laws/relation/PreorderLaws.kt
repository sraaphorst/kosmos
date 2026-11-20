package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes

/**
 * Preorder laws for a relation `≤`:
 * - [ReflexivityLaw]: `a ≤ a`
 * - [TransitivityLaw]: `a ≤ b ∧ b ≤ c ⇒ a ≤ c`
 */
class PreorderLaws<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "Preorder (${rel.symbol})"

    override val binding = LawBinding.of(this, rel, arb, pr)

    override fun children(): List<LawNode> = listOf(
        ReflexivityLaw(rel, arb, pr),
        TransitivityLaw(rel, arb, pr)
    ).asNodes()
}
