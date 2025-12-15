package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Monoid] Laws:
 * - [SemigroupLaws]
 * - [IdentityLaw]
 */
class MonoidLaws<A : Any>(
    private val monoid: Monoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Monoid", monoid.op.symbol)

    // This could be SemigroupLaws + NonAssociativeMonoidLaws but this would duplicate laws.
    override fun laws(): List<TestingLaw> =
        SemigroupLaws(monoid, arb, eq, pr).laws() +
            listOf(IdentityLaw(monoid.op, monoid.identity, arb, eq, pr))
}
