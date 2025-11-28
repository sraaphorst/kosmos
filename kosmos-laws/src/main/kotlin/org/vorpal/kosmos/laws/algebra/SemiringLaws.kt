package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a Semiring:
 *
 *  - All Hemiring laws:
 *      * (A, +, 0) is a commutative monoid
 *      * (A, ⋅) is a semigroup
 *      * ⋅ distributes over +
 *
 *  - Plus: (A, ⋅, 1) is a monoid
 */
class SemiringLaws<A : Any>(
    private val semiring: Semiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅"
) {

    fun laws(): List<TestingLaw> =
        // Hemiring: additive commutative monoid + multiplicative semigroup + distributivity
        HemiringLaws(
            hemiring = semiring,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                // Upgrade mul to a full monoid
                MonoidLaws(
                    monoid = semiring.mul,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = mulSymbol
                ).laws()
}