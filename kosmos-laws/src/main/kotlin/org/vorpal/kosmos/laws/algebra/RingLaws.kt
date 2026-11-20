package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Ring] laws:
 * - [RngLaws]
 * - [SemiringLaws]
 * - [NonAssociativeRingLaws]
 */
class RingLaws<A : Any>(
    private val ring: Ring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Ring", ring.add.op.symbol, ring.mul.op.symbol)

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            RngLaws(ring, arb, eq, pr),
            SemiringLaws(ring, arb, eq, pr),
            NonAssociativeRingLaws(ring, arb, eq, pr)
        ).asNodes()
}
