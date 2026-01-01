package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeSemigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeSemigroup] laws:
 * - [SemigroupLaws]
 * - [CommutativityLaw]
 */
class CommutativeSemigroupLaws<A : Any>(
    private val semigroup: CommutativeSemigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeSemigroup", semigroup.op.symbol)

    override fun laws(): List<TestingLaw> =
        SemigroupLaws(semigroup, arb, eq, pr).laws() +
            listOf(CommutativityLaw(semigroup.op, arb, eq, pr))
}
