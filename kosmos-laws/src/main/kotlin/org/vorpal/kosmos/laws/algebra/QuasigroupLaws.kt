package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Quasigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CancellativityLaw
import org.vorpal.kosmos.laws.property.LeftDivisionLaw
import org.vorpal.kosmos.laws.property.RightDivisionLaw
import org.vorpal.kosmos.laws.property.TotalityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Quasigroup] laws:
 * - [NonAssociativeSemigroupLaws]
 * - [TotalityLaw] on left division
 * - [TotalityLaw] on right division
 * - [LeftDivisionLaw] on left division
 * - [RightDivisionLaw] on right division
 * - [CancellativityLaw]
 */
class QuasigroupLaws<A : Any>(
    private val quasigroup: Quasigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName("Quasigroup",
        quasigroup.op.symbol, quasigroup.leftDiv.symbol, quasigroup.rightDiv.symbol)

    override fun laws(): List<TestingLaw> =
        NonAssociativeSemigroupLaws(quasigroup, arb, pr).laws() +
            listOf(
                TotalityLaw(quasigroup.leftDiv, arb, pr),
                TotalityLaw(quasigroup.rightDiv, arb, pr),
                LeftDivisionLaw(quasigroup.op, quasigroup.leftDiv, arb, eq, pr),
                RightDivisionLaw(quasigroup.op, quasigroup.rightDiv, arb, eq, pr),
                CancellativityLaw(quasigroup.op, arb, eq, pr)
            )
}
