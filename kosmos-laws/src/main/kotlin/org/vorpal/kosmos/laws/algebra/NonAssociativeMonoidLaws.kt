package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.IdentityLaw

/**
 * Laws for a NonAssociativeMonoid:
 *
 *  - There exists an identity element e such that:
 *      * e ⋆ a = a
 *      * a ⋆ e = a
 *
 *  No associativity is required.
 */
class NonAssociativeMonoidLaws<A : Any>(
    private val monoid: NonAssociativeMonoid<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
): TestingLaw {

    override suspend fun test() {

    }
}