package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Hemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.DistributivityLaw

/**
 * Laws for a Hemiring:
 *
 *  - Additive structure (commutative monoid):
 *      * associativity of +
 *      * identity 0
 *      * commutativity of +
 *
 *  - Multiplicative structure (semigroup):
 *      * associativity of ⋅
 *
 *  - Distributivity:
 *      * a ⋅ (b + c) = a⋅b + a⋅c
 *      * (b + c) ⋅ a = b⋅a + c⋅a
 */
class HemiringLaws<A : Any>(
    private val hemiring: Hemiring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅"
) {

    fun laws(): List<TestingLaw> =
        // additive commutative monoid (A, +, 0)
        CommutativeMonoidLaws(
            monoid = hemiring.add,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = addSymbol
        ).laws() +
                // multiplicative semigroup (A, ⋅)
                SemigroupLaws(
                    semigroup = hemiring.mul,
                    arb = arb,
                    eq = eq,
                    pr = pr,
                    symbol = mulSymbol
                ).laws() +
                // distributivity of ⋅ over +
                listOf(
                    DistributivityLaw(
                        mul = hemiring.mul.op,
                        add = hemiring.add.op,
                        arb = arb,
                        eq = eq
                    )
                )
}