package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Loop] laws:
 * - [QuasigroupLaws]
 * - [IdentityLaw]
 */
class LoopLaws<A : Any>(
    private val loop: Loop<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): StructuredLawSuite {

    override val name = suiteName("Loop", loop.op.symbol)

    override val binding = LawBinding.of(this, loop, arb, eq, pr)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            QuasigroupLaws(loop, arb, eq, pr).node(mode),
            IdentityLaw(loop.op, loop.identity, arb, eq, pr).asNode()
        )
}
