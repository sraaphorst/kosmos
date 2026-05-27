package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Truss
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.TernaryDistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Truss] laws:
 * - [AbelianHeapLaws] on ternary (full)
 * - [SemigroupLaws] on multiplication (full)
 * - [TernaryDistributivityLaw] of multiplication over ternary
 */
class TrussLaws<A : Any>(
    val truss: Truss<A>,
    val arb: Arb<A>,
    val eq: Eq<A> = Eq.default(),
    val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Truss", truss.add.op.symbol, truss.mul.op.symbol)

    override fun laws(): List<TestingLaw> =
        listOf(TernaryDistributivityLaw(truss.mul.op, truss.add.op, arb, eq, pr))

    override fun fullLaws(): List<TestingLaw> =
        AbelianHeapLaws(truss.add, arb, eq, pr).fullLaws() +
            SemigroupLaws(truss.mul, arb, eq, pr).fullLaws() +
            laws()
}
