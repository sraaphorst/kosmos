package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeMonoid] laws:
 * - [NonAssociativeSemigroupLaws]
 * - [IdentityLaw]
 */
class NonAssociativeMonoidLaws<A : Any>(
    private val monoid: NonAssociativeMonoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
): LawSuite {

    override val name = suiteName("NonAssociativeMonoid", monoid.op.symbol)

    override fun laws(): List<TestingLaw> =
        NonAssociativeSemigroupLaws(monoid, arb, pr).laws() + listOf(
            IdentityLaw(monoid.op, monoid.identity, arb, eq, pr)
        )
}
