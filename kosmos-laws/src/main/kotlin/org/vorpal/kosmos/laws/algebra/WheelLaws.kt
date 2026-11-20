package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Wheel
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Wheel] laws:
 * - [CommutativeMonoidLaws] on add
 * - [CommutativeMonoidLaws] on mul
 * -[InvolutionLaw] on inv
 */
class WheelLaws<A : Any>(
    wheel: Wheel<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName(
        "Wheel", wheel.add.op.symbol, wheel.mul.op.symbol, wheel.inv.symbol
    )

    override val binding = LawBinding.of(this, wheel, arb, eq, pr)

    private val addMonoidLaws = CommutativeMonoidLaws(wheel.add, arb, eq, pr)
    private val mulMonoidLaws = CommutativeMonoidLaws(wheel.mul, arb, eq, pr)
    private val involutionLaw = InvolutionLaw(wheel.inv, arb, eq, pr)

    private val structureLaws: List<LawComponent> =
        listOf(involutionLaw)

    override fun children(): List<LawNode> =
        listOf(addMonoidLaws.node(), mulMonoidLaws.node()) +
            structureLaws.asNodes()

}
