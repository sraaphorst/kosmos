package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Loop
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw

/**
 * Laws for a Loop:
 *
 *  - All quasigroup laws:
 *      * cancellativity (left & right)
 *      * left/right division compatibility
 *  - Two–sided identity element e:
 *      * e ⋆ a = a
 *      * a ⋆ e = a
 */
class LoopLaws<A : Any>(
    private val loop: Loop<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        QuasigroupLaws(
            quasigroup = loop,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        ).laws() + listOf(
            IdentityLaw(
                op = loop.op,
                identity = loop.identity,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}