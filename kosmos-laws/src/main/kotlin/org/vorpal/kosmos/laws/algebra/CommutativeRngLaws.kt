package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRng
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CommutativeRng] laws:
 * - [RngLaws]
 * - [CommutativityLaw] for multiplication
 */
class CommutativeRngLaws<A : Any>(
    private val rng: CommutativeRng<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : StructuredLawSuite {

    override val name = suiteName("CommutativeRng", rng.add.op.symbol, rng.mul.op.symbol)

    override val binding = LawBinding.of(this, rng, arb, eq, pr)

    private val rngLaws = RngLaws(rng, arb, eq, pr)

    private val structureLaws: List<TestingLaw> =
        listOf(CommutativityLaw(rng.mul.op, arb, eq, pr))

    override fun children(mode: LawMode): List<LawNode> =
        listOf(rngLaws.node(mode)) + structureLaws.asNodes()
}