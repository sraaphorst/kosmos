package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.NoZeroDivisorsLaw

/**
 * Laws for a DivisionRing ⟨A, +, *, 0, 1⟩.
 *
 * Conceptually:
 *  - All ring laws (additive abelian group, multiplicative monoid, distributivity)
 *  - Every non-zero element has a multiplicative inverse
 *  - No zero divisors
 *
 * No commutativity of * is assumed here.
 */
class DivisionRingLaws<A : Any>(
    private val divisionRing: DivisionRing<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅"
) {
    private val nonZeroArb: Arb<A> = arb.filter { s -> !eq.eqv(s, divisionRing.add.identity) }

    fun laws(): List<TestingLaw> {
        val add = divisionRing.add
        val mul = divisionRing.mul

        return RingLaws(
            ring = divisionRing,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() + listOf(
            // 1) Every non-zero element has a multiplicative inverse.
            // Since we pass nonZeroArb here, we can use inverse instead of inverseOrNull.
            InvertibilityLaw(
                op = mul.op,
                identity = mul.identity,
                arbAll = nonZeroArb,
                eq = eq,
                pr = pr,
                inverse = divisionRing.reciprocal,
                symbol = mulSymbol
            ),

            // 2) No zero divisors: if a⋅b = 0 then a = 0 or b = 0.
            //    Implemented via: for all non-zero a,b, a⋅b ≠ 0.
            NoZeroDivisorsLaw(
                op = mul.op,
                zero = add.identity,
                pairArb = Arb.pair(nonZeroArb, nonZeroArb),
                eq = eq,
                pr = pr,
                symbol = mulSymbol
            )
        )
    }
}