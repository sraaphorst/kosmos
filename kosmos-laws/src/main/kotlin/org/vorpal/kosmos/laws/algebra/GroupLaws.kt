package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.asInverseOrNull
import org.vorpal.kosmos.laws.suiteName

/**
 * [Group] laws:
 * - [MonoidLaws]
 * - [InvertibilityLaw]
 */
class GroupLaws<A : Any>(
    private val group: Group<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Group", group.op.symbol)

    override fun laws(): List<TestingLaw> =
        MonoidLaws(group, arb, eq, pr).laws() +
            listOf(InvertibilityLaw(group.op, group.identity, arb,group.inverse.asInverseOrNull(), eq, pr))
}
