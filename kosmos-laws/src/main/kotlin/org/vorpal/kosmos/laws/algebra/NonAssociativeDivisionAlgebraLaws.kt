package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeDivisionAlgebra] laws:
 * - [NonAssociativeInvolutiveRingLaws]
 * - [InvertibilityLaw]
 */
class NonAssociativeDivisionAlgebraLaws<A : Any>(
    algebra: NonAssociativeDivisionAlgebra<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "NonAssociativeDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol
    )

    override val binding = LawBinding.of(this, algebra, arb, eq, pr)

    private val nonAssociativeInvolutiveRingLaws: NonAssociativeInvolutiveRingLaws<A> by lazy {
        NonAssociativeInvolutiveRingLaws(algebra, arb, eq, pr)
    }

    private val structureLaws: List<LawComponent> =
        listOf(
            InvertibilityLaw(
                op = algebra.mul.op,
                identity = algebra.mul.identity,
                arb = arb,
                inverse = algebra.reciprocal,
                eq = eq,
                pr = pr,
                exclude = algebra.zero
            )
        )

    override fun children(): List<LawNode> =
        listOf(nonAssociativeInvolutiveRingLaws.node()) +
            structureLaws.asNodes()

}
