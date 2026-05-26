package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRng
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeRng] laws:
 * - [AbelianGroupLaws] on addition (full)
 * - [MagmaLaws] on multiplication (full)
 * - [DistributivityLaw] of multiplication over addition
 */
class NonAssociativeRngLaws<A : Any>(
    private val rng: NonAssociativeRng<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("NonAssociativeRng", rng.add.op.symbol, rng.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        listOf(DistributivityLaw(rng.mul.op, rng.add.op, arb, eq, pr))

    override fun fullLaws(): List<TestingLaw> =
        AbelianGroupLaws(rng.add, arb, eq, pr).fullLaws() +
            MagmaLaws(rng.mul, arb, pr).fullLaws() +
            laws()
}
