package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeSemigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawMode
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
): StructuredLawSuite {

    override val name = suiteName("CommutativeSemigroup", semigroup.op.symbol)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            SemigroupLaws(semigroup, arb, eq, pr).node(mode),
            CommutativityLaw(semigroup.op, arb, eq, pr).asNode()
        )
}
