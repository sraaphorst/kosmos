package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeMonoid] laws:
 * - [MonoidLaws]
 * - [CommutativityLaw]
 */
class CommutativeMonoidLaws<A : Any>(
    private val monoid: CommutativeMonoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("CommutativeMonoid", monoid.op.symbol)

    override fun laws(): List<TestingLaw> =
        MonoidLaws(monoid, arb, eq, pr).laws() +
            listOf(CommutativityLaw(monoid.op, arb, eq, pr))
}
