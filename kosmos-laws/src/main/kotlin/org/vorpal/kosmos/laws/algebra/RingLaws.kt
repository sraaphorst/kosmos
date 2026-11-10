package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Ring] laws:
 * - [RngLaws]
 * - [IdentityLaw] on multiplication
 */
class RingLaws<A : Any>(
    ring: Ring<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): StructuredLawSuite {

    override val name = suiteName("Ring", ring.add.op.symbol, ring.mul.op.symbol)

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    private val rngLaws = RngLaws(ring, arb, eq, pr)
    private val identityLaw = IdentityLaw(ring.mul.op, ring.mul.identity, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(identityLaw)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(rngLaws.node(mode)) + structureLaws.asNodes()
}
