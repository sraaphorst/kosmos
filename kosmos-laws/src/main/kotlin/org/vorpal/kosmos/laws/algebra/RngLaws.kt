package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Rng
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.asInverseOrNull
import org.vorpal.kosmos.laws.suiteName

/**
 * [Rng] laws:
 * - [HemiringLaws]
 * - [InvertibilityLaw] for addition
 */
class RngLaws<A : Any>(
    private val rng: Rng<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Rng", rng.add.op.symbol, rng.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        HemiringLaws(rng, arb, eq, pr).laws() +
            listOf(InvertibilityLaw(rng.add.op, rng.add.identity, arb, rng.add.inverse.asInverseOrNull(), eq, pr))
}
