package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semiring] laws:
 * - [HemiringLaws]
 * - [IdentityLaw] for multiplication
 */
class SemiringLaws<A : Any>(
    semiring: Semiring<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): StructuredLawSuite {
    override val name = suiteName("Semiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    override val binding = LawBinding.of(this, semiring, arb, eq, pr)

    private val hemiringLaws = HemiringLaws(semiring, arb, eq, pr)
    private val identityLaw = IdentityLaw(semiring.mul.op, semiring.mul.identity, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(identityLaw)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(hemiringLaws.node(mode)) + structureLaws.asNodes()
}
