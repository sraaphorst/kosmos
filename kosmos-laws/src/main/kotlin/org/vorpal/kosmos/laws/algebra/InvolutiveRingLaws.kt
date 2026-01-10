package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
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
): LawSuite {

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

    override fun laws(): List<TestingLaw> =
        nonAssociativeInvolutiveRingLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        nonAssociativeInvolutiveRingLaws.fullLaws() + structureLaws
}
