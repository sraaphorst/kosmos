package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Hemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AnnihilationLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Hemiring] laws:
 * - [CommutativeMonoidLaws] over addition (full)
 * - [SemigroupLaws] over multiplication (full)
 * - [DistributivityLaw] of multiplication over addition
 * - [AnnihilationLaw] of the additive identity with respect to multiplication
 */
class HemiringLaws<A : Any>(
    private val hemiring: Hemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Hemiring", hemiring.add.op.symbol, hemiring.mul.op.symbol)

    private val structureLaws: List<TestingLaw> =
        listOf(
            AnnihilationLaw(hemiring.mul.op, hemiring.add.identity, arb, eq, pr),
            DistributivityLaw(hemiring.mul.op, hemiring.add.op, arb, eq, pr)
        )

    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        CommutativeMonoidLaws(hemiring.add, arb, eq, pr).fullLaws() +
            SemigroupLaws(hemiring.mul, arb, eq, pr).fullLaws() +
            laws()
}
