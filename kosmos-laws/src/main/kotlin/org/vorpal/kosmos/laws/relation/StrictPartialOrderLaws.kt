package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes

/**
 * Strict partial order laws for a relation `<`:
 * - [IrreflexivityLaw]: `¬(a < a)`
 * - [TransitivityLaw]: `a < b ∧ b < c ⇒ a < c`
 *
 * Asymmetry follows from the two.
 */
class StrictPartialOrderLaws<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "StrictPartialOrder (${rel.symbol})"

    override val binding = LawBinding.of(this, rel, arb, pr)

    override fun children(): List<LawNode> = listOf(
        IrreflexivityLaw(rel, arb, pr),
        TransitivityLaw(rel, arb, pr)
    ).asNodes()
}
