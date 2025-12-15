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
    private val semiring: CommutativeSemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeSemiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        SemiringLaws(semiring, arb, eq, pr).laws() +
            listOf(CommutativityLaw(semiring.mul.op, arb, eq, pr))
}
