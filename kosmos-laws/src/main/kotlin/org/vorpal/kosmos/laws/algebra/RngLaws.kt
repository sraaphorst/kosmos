package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Rng] laws:
 * - [HemiringLaws]
 * - [AbelianGroupLaws] on addition
 */
class RngLaws<A : Any>(
    private val rng: Rng<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Rng", rng.add.op.symbol, rng.mul.op.symbol)

    override val binding = LawBinding.of(this, rng, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            HemiringLaws(rng, arb, eq, pr),
            AbelianGroupLaws(rng.add, arb, eq, pr)
        ).asNodes()
}
