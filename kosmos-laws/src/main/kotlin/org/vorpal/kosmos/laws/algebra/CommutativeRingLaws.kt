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
    ring: CommutativeRing<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeRing", ring.add.op.symbol, ring.mul.op.symbol)

    private val commutativeRngLaws = CommutativeRngLaws(ring, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(IdentityLaw(ring.mul.op, ring.mul.identity, arb, eq, pr))

    override fun laws(): List<TestingLaw> =
        commutativeRngLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        commutativeRngLaws.fullLaws() + structureLaws
}
