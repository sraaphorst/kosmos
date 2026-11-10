package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Hemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.AnnihilationLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Hemiring] laws:
 * - [CommutativeMonoidLaws] over addition (full)
 * - [SemigroupLaws] over multiplication (full)
 * - [DistributivityLaw] of multiplication over addition
 * - [AnnihilationLaw] of the additive identity with respect to multiplication
 */
class HemiringLaws<A : Any>(
    private val hemiring: Hemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): StructuredLawSuite {
    override val name = suiteName("Hemiring", hemiring.add.op.symbol, hemiring.mul.op.symbol)

    private val structureLaws: List<TestingLaw> =
        listOf(
            AnnihilationLaw(hemiring.mul.op, hemiring.add.identity, arb, eq, pr),
            DistributivityLaw(hemiring.mul.op, hemiring.add.op, arb, eq, pr)
        )

    /**
     * Compact: only distributivity and annihilation.
     * Full: the full additive commutative monoid and multiplicative semigroup graphs come first.
     */
    override fun children(mode: LawMode): List<LawNode> =
        when (mode) {
            LawMode.COMPACT -> structureLaws.asNodes()
            LawMode.FULL -> listOf(
                CommutativeMonoidLaws(hemiring.add, arb, eq, pr).node(LawMode.FULL),
                SemigroupLaws(hemiring.mul, arb, eq, pr).node(LawMode.FULL)
            ) + structureLaws.asNodes()
        }
}
