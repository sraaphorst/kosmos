package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw

/**
 * Laws for a CommutativeRing<A>:
 *
 *  - All Ring laws:
 *      * (A, +, 0) is an abelian group
 *      * (A, ⋆, 1) is a monoid
 *      * ⋆ distributes over +
 *
 *  - Extra commutativity:
 *      * a ⋆ b = b ⋆ a  for all a, b
 */
class CommutativeRingLaws<A : Any>(
    private val ring: CommutativeRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        RingLaws(
            ring = ring,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() + listOf(
            CommutativityLaw(
                op = ring.mul.op,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = mulSymbol
            )
        )
}