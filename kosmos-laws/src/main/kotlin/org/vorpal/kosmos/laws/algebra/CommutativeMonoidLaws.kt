package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNode
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
): StructuredLawSuite {

    override val name = suiteName("CommutativeMonoid", monoid.op.symbol)

    override val binding = LawBinding.of(this, monoid, arb, eq, pr)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            MonoidLaws(monoid, arb, eq, pr).node(mode),
            CommutativityLaw(monoid.op, arb, eq, pr).asNode()
        )
}
