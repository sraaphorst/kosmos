package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
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
): StructuredLawSuite {

    override val name = suiteName("NonAssociativeRing",
        ring.add.op.symbol, ring.mul.op.symbol)

    private val structureLaws: List<TestingLaw> =
        listOf(DistributivityLaw(ring.mul.op, ring.add.op, arb, eq, pr))

    /**
     * Compact: only distributivity.
     * Full: the full additive abelian group and multiplicative non-associative monoid graphs come first.
     */
    override fun children(mode: LawMode): List<LawNode> =
        when (mode) {
            LawMode.COMPACT -> structureLaws.asNodes()
            LawMode.FULL -> listOf(
                AbelianGroupLaws(ring.add, arb, eq, pr).node(LawMode.FULL),
                NonAssociativeMonoidLaws(ring.mul, arb, eq, pr).node(LawMode.FULL)
            ) + structureLaws.asNodes()
        }
}
