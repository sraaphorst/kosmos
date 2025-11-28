package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw

/**
 * Laws for an Abelian (commutative) group:
 *  - all group laws
 *  - commutativity of the operation
 */
class AbelianGroupLaws<A : Any>(
    private val group: Group<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        GroupLaws(
            group = group,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        ).laws() + listOf(
            CommutativityLaw(
                op = group.op,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}