package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
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
): LawSuite {

    override val name = suiteName("Loop", loop.op.symbol)

    override fun laws(): List<TestingLaw> =
        QuasigroupLaws(loop, arb, eq, pr).laws() +
            listOf(IdentityLaw(loop.op, loop.identity, arb, eq, pr))
}
