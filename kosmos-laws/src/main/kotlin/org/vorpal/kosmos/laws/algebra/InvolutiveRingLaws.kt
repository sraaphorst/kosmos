package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
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
) : LawSuite {

    override val name = suiteName(
        "InvolutiveRing",
        ring.add.op.symbol,
        ring.mul.op.symbol,
        ring.conj.symbol
    )

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    private val nonAssociativeInvolutiveRingLaws = NonAssociativeInvolutiveRingLaws(
        ring, arb, eq, pr
    )

    private val structureLaws: List<LawComponent> = listOf(
        AssociativityLaw(ring.mul.op, arb, eq, pr)
    )

    override fun children(): List<LawNode> =
        listOf(nonAssociativeInvolutiveRingLaws.node()) +
            structureLaws.asNodes()

}
