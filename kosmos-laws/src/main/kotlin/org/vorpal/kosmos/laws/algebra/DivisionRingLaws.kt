package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.DistinctIdentitiesLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [DivisionRing] laws:
 * - [RingLaws]
 * - [InvertibilityLaw] on multiplication, for every nonzero element
 * - [DistinctIdentitiesLaw] (0 ≠ 1)
 *
 * Every nonzero element (under [eq], so approximate rings decide what "zero" means) must have a two-sided
 * reciprocal; a missing reciprocal fails.
 */
class DivisionRingLaws<A : Any>(
    private val ring: DivisionRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName(
        "DivisionRing",
        ring.add.op.symbol,
        ring.mul.op.symbol,
        ring.reciprocal.symbol
    )

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            RingLaws(ring, arb, eq, pr),
            InvertibilityLaw(ring.mul.op, ring.mul.identity, arb, ring.reciprocal, eq, pr, exclude = ring.zero),
            DistinctIdentitiesLaw(ring.add.identity, ring.mul.identity, eq, pr)
        ).asNodes()
}
