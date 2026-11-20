package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Loop] laws:
 * - [QuasigroupLaws]
 * - [NonAssociativeMonoidLaws]
 */
class LoopLaws<A : Any>(
    private val loop: Loop<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Loop", loop.op.symbol)

    override val binding = LawBinding.of(this, loop, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            QuasigroupLaws(loop, arb, eq, pr),
            NonAssociativeMonoidLaws(loop, arb, eq, pr)
        ).asNodes()
}
