package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Ring] laws:
 * - [RngLaws]
 * - [IdentityLaw] on multiplication
 */
class RingLaws<A : Any>(
    private val ring: Ring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Ring", ring.add.op.symbol, ring.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        RngLaws(ring, arb, eq, pr).laws() +
            listOf(IdentityLaw(ring.mul.op, ring.mul.identity, arb, eq, pr))
}
