package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeRing] laws:
 * - [RingLaws]
 * - [CommutativeRngLaws]
 * - [CommutativeSemiringLaws]
 */
class CommutativeRingLaws<A : Any>(
    private val ring: CommutativeRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("CommutativeRing", ring.add.op.symbol, ring.mul.op.symbol)

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            RingLaws(ring, arb, eq, pr),
            CommutativeRngLaws(ring, arb, eq, pr),
            CommutativeSemiringLaws(ring, arb, eq, pr)
        ).asNodes()
}
