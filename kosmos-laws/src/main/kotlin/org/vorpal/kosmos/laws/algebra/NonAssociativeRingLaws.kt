package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeRing] laws:
 * - [AbelianGroupLaws] on addition (full)
 * - [NonAssociativeMonoidLaws] on multiplication (full)
 * - [DistributivityLaw] of multiplication over addition
 */
class NonAssociativeRingLaws<A : Any>(
    private val algebra: NonAssociativeRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("NonAssociativeAlgebra",
        algebra.add.op.symbol, algebra.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        listOf(DistributivityLaw(algebra.mul.op, algebra.add.op, arb, eq, pr))

    override fun fullLaws(): List<TestingLaw> =
        AbelianGroupLaws(algebra.add, arb, eq, pr).fullLaws() +
            NonAssociativeMonoidLaws(algebra.mul, arb, eq, pr).fullLaws() +
            laws()
}
