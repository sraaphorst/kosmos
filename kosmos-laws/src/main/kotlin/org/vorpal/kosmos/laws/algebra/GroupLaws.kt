package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Group] laws:
 * - [MonoidLaws]
 * - [LoopLaws] (a group is a loop: division is multiplication by an inverse)
 * - [InvertibilityLaw]
 */
class GroupLaws<A : Any>(
    private val group: Group<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Group", group.op.symbol)

    override val binding = LawBinding.of(this, group, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            MonoidLaws(group, arb, eq, pr),
            LoopLaws(group, arb, eq, pr),
            InvertibilityLaw(group.op, group.identity, arb, group.inverse, eq, pr)
        ).asNodes()
}
