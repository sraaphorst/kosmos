package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeSemiring] laws:
 * - [SemiringLaws]
 * - [CommutativeMonoidLaws] on multiplication
 */
class CommutativeSemiringLaws<A : Any>(
    private val semiring: CommutativeSemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("CommutativeSemiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    override val binding = LawBinding.of(this, semiring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            SemiringLaws(semiring, arb, eq, pr),
            CommutativeMonoidLaws(semiring.mul, arb, eq, pr)
        ).asNodes()
}
