package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a Ring:
 *
 *  - As a Semiring:
 *      * (A, +, 0) is a commutative monoid
 *      * (A, ⋆, 1) is a monoid
 *      * ⋆ distributes over +
 *
 *  - Extra ring structure:
 *      * (A, +, 0) is actually an abelian group
 *          - every element has an additive inverse
 */
class RingLaws<A : Any>(
    private val ring: Ring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        // Semiring structure: commutative monoid under +, monoid under ⋆, distributivity
        SemiringLaws(
            semiring = ring,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +

                // Upgrade (A, +, 0) from commutative monoid to abelian group
                AbelianGroupLaws(
                    group = ring.add,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = addSymbol
                ).laws()
}