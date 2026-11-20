package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [AbelianGroup] laws:
 * - [GroupLaws]
 * - [CommutativeMonoidLaws]
 */
class AbelianGroupLaws<A : Any>(
    private val group: AbelianGroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("AbelianGroup", group.op.symbol)

    override val binding = LawBinding.of(this, group, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            GroupLaws(group, arb, eq, pr),
            CommutativeMonoidLaws(group, arb, eq, pr)
        ).asNodes()
}
