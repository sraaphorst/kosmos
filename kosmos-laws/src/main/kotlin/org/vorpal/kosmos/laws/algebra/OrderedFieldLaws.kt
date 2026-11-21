package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.NegativeScalingLaw
import org.vorpal.kosmos.laws.property.PositiveScalingLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * Laws of an ordered field (e.g. ℚ, ℝ): a field with a compatible total order `≤`.
 *
 * - [FieldLaws]
 * - [OrderedCommutativeRingLaws] (the order axioms)
 *
 * Derived (a field has no zero divisors), and checked so that a failure points at the order arithmetic directly;
 * `<` is `≤` and not equal:
 * - [PositiveScalingLaw]: `a < b ∧ 0 < c ⇒ ac < bc`
 * - [NegativeScalingLaw]: `a < b ∧ c < 0 ⇒ bc < ac`
 *
 * Kosmos has no ordered-field structure type, so the order is passed alongside the field.
 */
class OrderedFieldLaws<A : Any>(
    private val field: Field<A>,
    private val order: TotalOrder<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName(
        "OrderedField", field.add.op.symbol, field.mul.op.symbol, field.reciprocal.symbol, order.relation.symbol
    )

    override val binding = LawBinding.of(this, field, order, arb, eq, pr)

    override fun children(): List<LawNode> = listOf(
        FieldLaws(field, arb, eq, pr),
        OrderedCommutativeRingLaws(field, order, arb, eq, pr),
        PositiveScalingLaw(field.mul.op, field.zero, field.add.inverse, order.relation, arb, eq, pr),
        NegativeScalingLaw(field.mul.op, field.zero, field.add.inverse, order.relation, arb, eq, pr)
    ).asNodes()
}
