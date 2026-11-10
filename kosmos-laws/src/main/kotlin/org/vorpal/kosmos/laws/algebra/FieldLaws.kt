package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.property.DistinctIdentitiesLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Field] laws:
 * - [DivisionRingLaws]
 * - [CommutativityLaw] for multiplication
 * - [DistinctIdentitiesLaw]
 */
class FieldLaws<A : Any>(
    field: Field<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : StructuredLawSuite {

    override val name = suiteName("Field", field.add.op.symbol, field.mul.op.symbol, field.reciprocal.symbol)

    private val divisionRingLaws = DivisionRingLaws(field, arb, eq, pr)

    private val structureLaws: List<TestingLaw> = listOf(
        CommutativityLaw(field.mul.op, arb, eq, pr),
        DistinctIdentitiesLaw(field.add.identity, field.mul.identity, eq, pr)
    )

    override fun children(mode: LawMode): List<LawNode> =
        listOf(divisionRingLaws.node(mode)) + structureLaws.asNodes()
}
