package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.suiteName

class NonAssociativeInvolutiveRingLaws<A : Any>(
    algebra: NonAssociativeInvolutiveRing<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : StructuredLawSuite {

    override val name = suiteName(
        "NonAssociativeInvolutiveRing",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol)

    private val nonAssociativeRingLaws = NonAssociativeRingLaws(algebra, arb, eq, pr)
    private val conjugationLaws = ConjugationLaws(algebra.conj, algebra.add, algebra.mul, arb, eq, pr)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(nonAssociativeRingLaws.node(mode), conjugationLaws.node(mode))

}
