package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeDivisionAlgebra] laws:
 * - [NonAssociativeInvolutiveRingLaws]
 * - [InvertibilityLaw]
 */
class NonAssociativeDivisionAlgebraLaws<A : Any>(
    algebra: NonAssociativeDivisionAlgebra<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "NonAssociativeDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol
    )

    private val nonAssociativeInvolutiveRingLaws: NonAssociativeInvolutiveRingLaws<A> by lazy {
        NonAssociativeInvolutiveRingLaws(algebra, arb, eq, pr)
    }

    private val reciprocalOrNull: UnaryOp<A, A?> =
        UnaryOp(algebra.reciprocal.symbol) { a ->
        if (eq(a, algebra.zero)) null
        else algebra.reciprocal(a)
    }

    private val structureLaws: List<TestingLaw> =
        listOf(
            InvertibilityLaw(
                op = algebra.mul.op,
                identity = algebra.mul.identity,
                arb = arb.filterNot { eq(it, algebra.zero) },
                inverseOrNull = reciprocalOrNull,
                eq = eq,
                pr = pr
            )
        )

    override fun laws(): List<TestingLaw> =
        nonAssociativeInvolutiveRingLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        nonAssociativeInvolutiveRingLaws.fullLaws() + structureLaws
}
