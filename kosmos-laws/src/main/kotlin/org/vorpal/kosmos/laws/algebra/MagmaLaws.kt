package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Magma
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.TotalityLaw
import org.vorpal.kosmos.laws.suiteName


/**
 * [Magma] laws:
 * - [TotalityLaw]
 *
 * Since no algebraic axioms are required, this suite checks the pragmatic invariant
 * that the operation is total over generated inputs (i.e. it does not throw).
 */
class MagmaLaws<A : Any>(
    private val magma: Magma<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
): StructuredLawSuite {

    override val name = suiteName("Magma", magma.op.symbol)

    override fun children(mode: LawMode): List<LawNode> =
        listOf(
            TotalityLaw(op = magma.op, arb = arb, pr = pr).asNode()
        )
}