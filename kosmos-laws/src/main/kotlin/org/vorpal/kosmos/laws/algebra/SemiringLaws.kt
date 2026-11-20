package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semiring] laws:
 * - [HemiringLaws]
 * - [MonoidLaws] on multiplication
 */
class SemiringLaws<A : Any>(
    private val semiring: Semiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Semiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    override val binding = LawBinding.of(this, semiring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            HemiringLaws(semiring, arb, eq, pr),
            MonoidLaws(semiring.mul, arb, eq, pr)
        ).asNodes()
}
