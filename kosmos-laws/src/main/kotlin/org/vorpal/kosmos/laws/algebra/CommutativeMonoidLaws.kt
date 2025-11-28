package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw

/**
 * Laws for a commutative monoid:
 *  - associativity
 *  - identity
 *  - commutativity
 */
class CommutativeMonoidLaws<A : Any>(
    private val monoid: CommutativeMonoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        MonoidLaws(
            monoid = monoid,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        ).laws() + listOf(
            CommutativityLaw(
                op = monoid.op,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}
