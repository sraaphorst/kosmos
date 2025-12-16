package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeSemiring] laws:
 * - [SemiringLaws]
 * - [CommutativityLaw] over multiplication
 */
class CommutativeSemiringLaws<A : Any>(
    semiring: CommutativeSemiring<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeSemiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    private val semiringLaws = SemiringLaws(semiring, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(CommutativityLaw(semiring.mul.op, arb, eq, pr))

    override fun laws(): List<TestingLaw> =
        semiringLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        semiringLaws.fullLaws() + structureLaws
}
