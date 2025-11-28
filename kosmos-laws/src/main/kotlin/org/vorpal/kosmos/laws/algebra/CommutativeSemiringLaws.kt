package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw

/**
 * Laws for a Commutative Semiring:
 *
 *  - All Semiring laws:
 *      * (A, +, 0) is a commutative monoid
 *      * (A, ⋅, 1) is a monoid
 *      * ⋅ distributes over +
 *
 *  - Plus:
 *      * Commutativity of multiplication: a ⋅ b = b ⋅ a
 */
class CommutativeSemiringLaws<A : Any>(
    private val semiring: CommutativeSemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅"
) {

    fun laws(): List<TestingLaw> =
        // All semiring laws
        SemiringLaws(
            semiring = semiring,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                // Plus commutativity of multiplication
                listOf(
                    CommutativityLaw(
                        op = semiring.mul.op,
                        arb = arb,
                        eq = eq,
                        pr = pr,
                        symbol = mulSymbol
                    )
                )
}