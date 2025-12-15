package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeRing] laws:
 * - [CommutativeRngLaws]
 * - [IdentityLaw] on multiplication
 */
class CommutativeRingLaws<A : Any>(
    private val ring: CommutativeRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeRing", ring.add.op.symbol, ring.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        CommutativeRngLaws(ring, arb, eq, pr).laws() +
            listOf(IdentityLaw(ring.mul.op, ring.mul.identity, arb, eq, pr))
}
