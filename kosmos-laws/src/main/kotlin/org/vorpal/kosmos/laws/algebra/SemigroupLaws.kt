package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw

/**
 * Convenience wrapper: “all the usual semigroup laws” packaged as TestingLaw instances.
 *
 * This doesn’t *run* anything by itself – it just builds the law objects.
 * Callers pass the resulting list to `runLaws`.
 */
class SemigroupLaws<A : Any>(
    private val semigroup: Semigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> =
        listOf(
            AssociativityLaw(
                op = semigroup.op,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = symbol
            )
        )
}