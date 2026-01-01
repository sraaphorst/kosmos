package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [BooleanAlgebra] laws:
 * - [DistributiveLatticeLaws]
 * - Various laws between operators
 */
class BooleanAlgebraLaws<A : Any>(
    algebra: BooleanAlgebra<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "BooleanAlgebra",
        algebra.join.symbol,
        algebra.meet.symbol,
        algebra.not.symbol
    )

    private val latticeLaws = DistributiveLatticeLaws(algebra, arb, eq, pr)

    private val complementAndNegationLaws: List<TestingLaw> = listOf(
        complementJoinTopLaw(algebra.join, algebra.not, algebra.top, arb, eq, pr),
        complementMeetBottomLaw(algebra.meet, algebra.not, algebra.bottom, arb, eq, pr),

        // Debug-friendly checks
        notBottomIsTopLaw(algebra.not, algebra.bottom, algebra.top, eq, pr),
        notTopIsBottomLaw(algebra.not, algebra.bottom, algebra.top, eq, pr),
        InvolutionLaw(algebra.not, arb, eq, pr),
        deMorganJoinLaw(algebra.join, algebra.meet, algebra.not, arb, eq, pr),
        deMorganMeetLaw(algebra.join, algebra.meet, algebra.not, arb, eq, pr),
    )

    override fun laws(): List<TestingLaw> =
        latticeLaws.laws() +
            complementAndNegationLaws

    override fun fullLaws(): List<TestingLaw> =
        latticeLaws.fullLaws() +
            complementAndNegationLaws
}