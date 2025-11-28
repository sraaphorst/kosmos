package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw

/**
 * Laws for a NonAssociativeAlgebra:
 *
 *  - (A, +, 0) is an abelian group
 *      * associativity, commutativity, identity, inverses for +
 *
 *  - (A, ⋆, 1) is a non-associative monoid
 *      * identity element 1 for ⋆ (no associativity required)
 *
 *  - ⋆ distributes over +
 *      * left:  a ⋆ (b + c) = a ⋆ b + a ⋆ c
 *      * right: (a + b) ⋆ c = a ⋆ c + b ⋆ c
 */
class NonAssociativeAlgebraLaws<A : Any>(
    private val algebra: NonAssociativeAlgebra<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        // (A, +, 0) is an abelian group
        AbelianGroupLaws(
            group = algebra.add,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = addSymbol
        ).laws() +
                // (A, ⋆, 1) is a non-associative monoid (identity only)
                NonAssociativeMonoidLaws(
                    monoid = algebra.mul,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = mulSymbol
                ).laws() +
                // Distributivity of ⋆ over +
                listOf(
                    DistributivityLaw(
                        mul = algebra.mul.op,
                        add = algebra.add.op,
                        arb = arb,
                        eq = eq,
                        pr = pr,
                        mulSymbol = mulSymbol,
                        addSymbol = addSymbol
                    )
                )
}