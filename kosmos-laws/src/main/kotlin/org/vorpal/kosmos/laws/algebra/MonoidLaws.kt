package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Monoid] Laws:
 * - [SemigroupLaws]
 * - [NonAssociativeMonoidLaws]
 */
class MonoidLaws<A : Any>(
    private val monoid: Monoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Monoid", monoid.op.symbol)

    override val binding = LawBinding.of(this, monoid, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            SemigroupLaws(monoid, arb, eq, pr),
            NonAssociativeMonoidLaws(monoid, arb, eq, pr)
        ).asNodes()
}
