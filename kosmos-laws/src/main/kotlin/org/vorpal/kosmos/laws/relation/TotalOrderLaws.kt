package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes

/**
 * Total order laws for a relation `≤`:
 * - [PartialOrderLaws]
 * - [StrongConnexityLaw]: `a ≤ b ∨ b ≤ a`
 */
class TotalOrderLaws<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "TotalOrder (${rel.symbol})"

    override val binding = LawBinding.of(this, rel, arb, eq, pr)

    override fun children(): List<LawNode> = listOf(
        PartialOrderLaws(rel, arb, eq, pr),
        StrongConnexityLaw(rel, arb, pr)
    ).asNodes()
}
