package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [DivisionRing] laws:
 * - [RingLaws]
 * - [InvertibilityLaw]
 *
 * Note: reciprocal is typically undefined at the additive identity, so we treat it as "no inverse".
 */
class DivisionRingLaws<A : Any>(
    ring: DivisionRing<A>,
    arb: Arb<A>,
    eq: Eq<A> = Eq.default(),
    pr: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "DivisionRing",
        ring.add.op.symbol,
        ring.mul.op.symbol,
        ring.reciprocal.symbol
    )

    private val ringLaws = RingLaws(ring, arb, eq, pr)

    private val reciprocalOrNull: UnaryOp<A, A?> =
        UnaryOp(ring.reciprocal.symbol) { a ->
            // Exclude zero (under the provided Eq, so approximate rings can decide what "zero" means).
            if (eq(a, ring.zero)) null
            else {
                // If the implementation throws on non-invertibles, treat it as "no inverse".
                try {
                    ring.reciprocal(a)
                } catch (_: Throwable) {
                    null
                }
            }
        }

    private val structureLaws: List<TestingLaw> = listOf(
        InvertibilityLaw(
            op = ring.mul.op,
            identity = ring.mul.identity,
            arb = arb,
            inverseOrNull = reciprocalOrNull,
            eq = eq,
            pr = pr
        )
    )

    override fun laws(): List<TestingLaw> =
        ringLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        ringLaws.fullLaws() + structureLaws
}
