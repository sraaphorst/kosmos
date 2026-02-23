package org.vorpal.kosmos.org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.suiteName
import org.vorpal.kosmos.org.vorpal.kosmos.laws.property.RestrictedInverseLaw

/**
 * [Meadow] laws:
 * - [CommutativeRingLaws]
 * - [InvolutionLaw] on inv
 * - [RestrictedInverseLaw] on mul and inv
 */
class MeadowLaws<A : Any>(
    meadow: Meadow<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Meadow",
        meadow.add.op.symbol, meadow.mul.op.symbol, meadow.inv.symbol
    )

    private val commutativeRingLaws = CommutativeRingLaws(meadow, arb, eq, pr)
    private val structureLaws: List<TestingLaw> = listOf(
        InvolutionLaw(meadow.inv, arb, eq, pr),
        RestrictedInverseLaw(meadow.mul.op, meadow.inv, arb, eq, pr)
    )

    override fun laws(): List<TestingLaw> =
        commutativeRingLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        commutativeRingLaws.fullLaws() + structureLaws
}
