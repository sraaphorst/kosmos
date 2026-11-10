package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [InvolutiveRing] laws:
 * - [NonAssociativeInvolutiveRingLaws]
 * - [AssociativityLaw]
 */
class InvolutiveRingLaws<A : Any>(
    ring: InvolutiveRing<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : StructuredLawSuite {

    override val name = suiteName(
        "InvolutiveRing",
        ring.add.op.symbol,
        ring.mul.op.symbol,
        ring.conj.symbol
    )

    private val nonAssociativeInvolutiveRingLaws = NonAssociativeInvolutiveRingLaws(
        ring, arb, eq, pr
    )

    private val structureLaws: List<TestingLaw> = listOf(
        AssociativityLaw(ring.mul.op, arb, eq, pr)
    )

    override fun children(mode: LawMode): List<LawNode> =
        listOf(nonAssociativeInvolutiveRingLaws.node(mode)) +
            structureLaws.asNodes()

}
