package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semigroup] laws:
 * - [NonAssociativeSemigroupLaws]
 * - [AssociativityLaw]
 */
class SemigroupLaws<A : Any>(
    private val semigroup: Semigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Semigroup", semigroup.op.symbol)

    override fun laws(): List<TestingLaw> =
        NonAssociativeSemigroupLaws(semigroup, arb, pr).laws() +
                listOf(AssociativityLaw(semigroup.op, arb, eq, pr))
}
