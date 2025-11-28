package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a Monoid:
 *
 *  - Non-associative monoid laws:
 *      * identity element e such that e ⋆ a = a and a ⋆ e = a
 *
 *  - Plus semigroup laws:
 *      * associativity: (a ⋆ b) ⋆ c = a ⋆ (b ⋆ c)
 */
class MonoidLaws<A : Any>(
    private val monoid: Monoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        NonAssociativeMonoidLaws(
            monoid = monoid,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        ).laws() +
                SemigroupLaws(
                    semigroup = monoid,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = symbol
                ).laws()
}