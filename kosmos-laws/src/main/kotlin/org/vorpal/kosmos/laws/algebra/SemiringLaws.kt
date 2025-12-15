package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Semiring] laws:
 * - [HemiringLaws]
 * - [IdentityLaw] for multiplication
 */
class SemiringLaws<A : Any>(
    private val semiring: Semiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Semiring", semiring.add.op.symbol, semiring.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        // Hemiring: additive commutative monoid + multiplicative semigroup + distributivity
        HemiringLaws(semiring, arb, eq, pr).laws() +
            listOf(IdentityLaw(semiring.mul.op, semiring.mul.identity, arb, eq, pr))
}
