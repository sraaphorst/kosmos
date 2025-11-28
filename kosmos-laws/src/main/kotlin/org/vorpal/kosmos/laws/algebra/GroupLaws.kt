package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw

/**
 * Laws for a Group:
 *
 *  - Underlying monoid laws:
 *      * associativity: (ab)c = a(bc)
 *      * identity: e a = a e = a
 *  - Invertibility:
 *      * for all a, a⁻¹: a a⁻¹ = e and a⁻¹ a = e
 *
 * We reuse [MonoidLaws] for the monoid part and add a total [InvertibilityLaw].
 */
class GroupLaws<A : Any>(
    private val group: Group<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        // Monoid structure: associativity + identity
        MonoidLaws(
            monoid = group,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        ).laws() + listOf(
            // Every element has an inverse with respect to `group.op`
            InvertibilityLaw(
                op = group.op,
                identity = group.identity,
                arbAll = arb,
                eq = eq,
                inverse = { a -> group.inverse(a) },
                pr = pr,
                symbol = symbol
            )
        )
}