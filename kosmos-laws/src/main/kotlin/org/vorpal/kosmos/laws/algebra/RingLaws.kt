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
    ring: Ring<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Ring", ring.add.op.symbol, ring.mul.op.symbol)

    private val rngLaws = RngLaws(ring, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(IdentityLaw(ring.mul.op, ring.mul.identity, arb, eq, pr))

    override fun laws(): List<TestingLaw> =
        rngLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        rngLaws.fullLaws() + structureLaws
}
