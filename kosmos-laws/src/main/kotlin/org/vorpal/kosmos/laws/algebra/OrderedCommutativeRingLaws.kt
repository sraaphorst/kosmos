package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.AdditiveMonotonicityLaw
import org.vorpal.kosmos.laws.property.NonnegativeProductLaw
import org.vorpal.kosmos.laws.relation.TotalOrderLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * Laws of a commutative ring with a compatible total order `≤` (e.g. ℤ, ℚ).
 *
 * The axioms:
 * - [CommutativeRingLaws]
 * - [TotalOrderLaws] on `≤`
 * - [AdditiveMonotonicityLaw]: `a ≤ b ⇒ a + c ≤ b + c`
 * - [NonnegativeProductLaw]: `0 ≤ a ∧ 0 ≤ b ⇒ 0 ≤ ab`
 *
 * Strict scaling (`a < b ∧ 0 < c ⇒ ac < bc`) does not follow without "no zero divisors"; [OrderedFieldLaws] adds it.
 *
 * Kosmos has no ordered-ring structure type, so the order is passed alongside the ring.
 */
class OrderedCommutativeRingLaws<A : Any>(
    private val ring: CommutativeRing<A>,
    private val order: TotalOrder<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName("OrderedCommutativeRing", ring.add.op.symbol, ring.mul.op.symbol, order.relation.symbol)

    override val binding = LawBinding.of(this, ring, order, arb, eq, pr)

    override fun children(): List<LawNode> {
        val le = order.relation
        return listOf(
            CommutativeRingLaws(ring, arb, eq, pr),
            TotalOrderLaws(le, arb, eq, pr),
            AdditiveMonotonicityLaw(ring.add.op, le, arb, pr),
            NonnegativeProductLaw(ring.mul.op, ring.zero, ring.add.inverse, le, arb, pr)
        ).asNodes()
    }
}
