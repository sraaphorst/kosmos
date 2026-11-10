package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semigroup] laws:
 * - [MagmaLaws]
 * - [AssociativityLaw]
 */
class SemigroupLaws<A : Any>(
    private val semigroup: Semigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): StructuredLawSuite {

    override val name = suiteName("Semigroup", semigroup.op.symbol)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            MagmaLaws(semigroup, arb, pr).node(mode),
            AssociativityLaw(semigroup.op, arb, eq, pr).asNode()
        )
}
