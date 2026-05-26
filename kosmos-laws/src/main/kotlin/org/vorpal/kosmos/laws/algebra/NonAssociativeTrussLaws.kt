package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeTruss
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.TernaryDistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeTruss] laws:
 * - [AbelianHeapLaws] on ternary (full)
 * - [MagmaLaws] on multiplication (full)
 * - [TernaryDistributivityLaw] of multiplication over ternary
 */
class NonAssociativeTrussLaws<A : Any>(
    val truss: NonAssociativeTruss<A>,
    val arb: Arb<A>,
    val eq: Eq<A> = Eq.default(),
    val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("NonAssociativeTruss",
        truss.add.op.symbol, truss.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        listOf(TernaryDistributivityLaw(truss.mul.op, truss.add.op, arb, eq, pr))

    override fun fullLaws(): List<TestingLaw> =
        AbelianHeapLaws(truss.add, arb, eq, pr).fullLaws() +
            MagmaLaws(truss.mul, arb, pr).fullLaws() +
            laws()
}
