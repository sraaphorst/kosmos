package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.asInverseOrNull
import org.vorpal.kosmos.laws.suiteName

/**
 * [Rng] laws:
 * - [HemiringLaws]
 * - [InvertibilityLaw] for addition
 */
class RngLaws<A : Any>(
    rng: Rng<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): StructuredLawSuite {

    override val name = suiteName("Rng", rng.add.op.symbol, rng.mul.op.symbol)

    private val hemiringLaws = HemiringLaws(rng, arb, eq, pr)
    private val invertibilityLaw = InvertibilityLaw(
        rng.add.op, rng.add.identity, arb, rng.add.inverse.asInverseOrNull(), eq, pr
    )

    private val structureLaws: List<TestingLaw> =
        listOf(invertibilityLaw)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(hemiringLaws.node(mode)) + structureLaws.asNodes()
}
