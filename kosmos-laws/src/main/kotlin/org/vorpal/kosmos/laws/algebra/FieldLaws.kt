package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw

/**
 * Laws for a Field ⟨A, +, *, 0, 1⟩.
 *
 * A Field is just:
 *  - A DivisionRing with
 *  - Commutative multiplication.
 */
class FieldLaws<A : Any>(
    private val field: Field<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅"
) {

    fun laws(): List<TestingLaw> {
        val mul = field.mul

        return DivisionRingLaws(
            divisionRing = field,
            arb = arb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() + listOf(
            // Extra field law: multiplication is commutative.
            CommutativityLaw(
                op = mul.op,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = mulSymbol
            )
        )
    }
}