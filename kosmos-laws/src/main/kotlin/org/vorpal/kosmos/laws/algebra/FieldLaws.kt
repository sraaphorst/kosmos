package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Field] laws:
 * - [DivisionRingLaws]
 * - [CommutativeRingLaws]
 */
class FieldLaws<A : Any>(
    private val field: Field<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName("Field", field.add.op.symbol, field.mul.op.symbol, field.reciprocal.symbol)

    override val binding = LawBinding.of(this, field, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            DivisionRingLaws(field, arb, eq, pr),
            CommutativeRingLaws(field, arb, eq, pr)
        ).asNodes()
}
