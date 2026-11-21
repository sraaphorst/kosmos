package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.AbsDefiniteLaw
import org.vorpal.kosmos.laws.property.AbsFromOrderLaw
import org.vorpal.kosmos.laws.property.AbsMultiplicativeLaw
import org.vorpal.kosmos.laws.property.AbsNonnegativeLaw
import org.vorpal.kosmos.laws.property.TriangleInequalityLaw

/**
 * Laws of the absolute value `|·|` of an ordered commutative ring (e.g. ℤ, ℚ), valued in the ring itself.
 *
 * The absolute value axioms:
 * - [AbsNonnegativeLaw]: `0 ≤ |a|`
 * - [AbsDefiniteLaw]: `|a| = 0 ⇔ a = 0`
 * - [AbsMultiplicativeLaw]: `|ab| = |a||b|`
 * - [TriangleInequalityLaw]: `|a + b| ≤ |a| + |b|`
 *
 * And the definition in an ordered ring:
 * - [AbsFromOrderLaw]: `|a| = a` if `0 ≤ a`, else `-a`
 *
 * The ring and order laws themselves are checked by [OrderedCommutativeRingLaws]; register both.
 */
class AbsoluteValueLaws<A : Any>(
    private val ring: CommutativeRing<A>,
    private val order: TotalOrder<A>,
    private val abs: Endo<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "AbsoluteValue (${abs.symbol})"

    override val binding = LawBinding.of(this, ring, order, abs, arb, eq, pr)

    override fun children(): List<LawNode> {
        val le = order.relation
        return listOf(
            AbsNonnegativeLaw(abs, ring.zero, le, arb, pr),
            AbsDefiniteLaw(abs, ring.zero, arb, eq, pr),
            AbsMultiplicativeLaw(abs, ring.mul.op, arb, eq, pr),
            TriangleInequalityLaw(abs, ring.add.op, le, arb, pr),
            AbsFromOrderLaw(abs, ring.zero, ring.add.inverse, le, arb, eq, pr)
        ).asNodes()
    }
}
