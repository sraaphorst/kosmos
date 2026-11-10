package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Quasigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.property.CancellativityLaw
import org.vorpal.kosmos.laws.property.LeftDivisionLaw
import org.vorpal.kosmos.laws.property.RightDivisionLaw
import org.vorpal.kosmos.laws.property.TotalityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Quasigroup] laws:
 * - [MagmaLaws]
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
): StructuredLawSuite {

    override val name = suiteName("Quasigroup",
        quasigroup.op.symbol, quasigroup.leftDiv.symbol, quasigroup.rightDiv.symbol)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            MagmaLaws(quasigroup, arb, pr).node(mode),
            TotalityLaw(quasigroup.leftDiv, arb, pr).asNode(),
            TotalityLaw(quasigroup.rightDiv, arb, pr).asNode(),
            LeftDivisionLaw(quasigroup.op, quasigroup.leftDiv, arb, eq, pr).asNode(),
            RightDivisionLaw(quasigroup.op, quasigroup.rightDiv, arb, eq, pr).asNode(),
            CancellativityLaw(quasigroup.op, arb, eq, pr).asNode()
        )
}
