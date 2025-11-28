package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for an InvolutiveRing<A>, i.e. a Ring with a conjugation:
 *
 *  - All Ring laws:
 *      * (A, +, 0) is an abelian group
 *      * (A, ⋆, 1) is a monoid
 *      * ⋆ distributes over +
 *
 *  - All InvolutiveAlgebra laws:
 *      * (a*)* = a
 *      * (a + b)* = a* + b*
 *      * (ab)* = b* a*
 *      * 1* = 1
 */
class InvolutiveRingLaws<A : Any>(
    private val ring: InvolutiveRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆",
    private val starSymbol: String = "*"
) {

    fun laws(): List<TestingLaw> =
        RingLaws(
            ring = ring,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                InvolutiveAlgebraLaws(
                    algebra = ring,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    addSymbol = addSymbol,
                    mulSymbol = mulSymbol,
                    starSymbol = starSymbol
                ).laws()
}