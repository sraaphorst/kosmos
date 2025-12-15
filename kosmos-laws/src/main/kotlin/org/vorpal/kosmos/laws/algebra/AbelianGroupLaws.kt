package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [AbelianGroup] Laws:
 *  - [GroupLaws]
 *  - [CommutativityLaw]
 */
class AbelianGroupLaws<A : Any>(
    private val group: AbelianGroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("AbelianGroup", group.op.symbol)

    override fun laws(): List<TestingLaw> =
        GroupLaws(group, arb, eq, pr).laws() +
            listOf(CommutativityLaw(group.op, arb, eq, pr))
}
