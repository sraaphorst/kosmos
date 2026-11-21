package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeRing] laws:
 * - [AbelianGroupLaws] on addition (full)
 * - [NonAssociativeMonoidLaws] on multiplication (full)
 * - [DistributivityLaw] of multiplication over addition
 */
class NonAssociativeRingLaws<A : Any>(
    private val ring: NonAssociativeRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("NonAssociativeRing",
        ring.add.op.symbol, ring.mul.op.symbol)

    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    private val structureLaws: List<LawComponent> =
        listOf(DistributivityLaw(ring.mul.op, ring.add.op, arb, eq, pr))

    /**
     * Compact: only distributivity.
     * Full: the full additive abelian group and multiplicative non-associative monoid graphs come first.
     */
    override fun children(): List<LawNode> =
        listOf(
            AbelianGroupLaws(ring.add, arb, eq, pr).node(),
            NonAssociativeMonoidLaws(ring.mul, arb, eq, pr).node()
        ) + structureLaws.asNodes()
}
