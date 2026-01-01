package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semiring] laws:
 * - [HemiringLaws]
 * - [IdentityLaw] for multiplication
 */
class SemiringLaws<A : Any>(
    semiring: Semiring<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Semiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    private val hemiringLaws = HemiringLaws(semiring, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(IdentityLaw(semiring.mul.op, semiring.mul.identity, arb, eq, pr))

    override fun laws(): List<TestingLaw> =
        hemiringLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        hemiringLaws.fullLaws() + structureLaws
}
