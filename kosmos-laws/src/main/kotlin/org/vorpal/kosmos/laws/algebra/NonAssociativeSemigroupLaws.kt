package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeSemigroup
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.TotalityLaw

/**
 * Given a `NonAssociativeSemigroup`, check to see that it is valid, i.e. that it obeys
 * the `TotalityLaw`, which dictates that evaluating `a · b` does not throw.
 */
class NonAssociativeSemigroupLaws<A : Any>(
    private val s: NonAssociativeSemigroup<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default(),
) : LawSuite {

    override val name = "NonAssociativeSemigroup"

    override fun laws(): List<TestingLaw> =
        listOf(
            // recommended pragmatic check:
            TotalityLaw(op = s.op, arb = arb, pr = pr)
        )
}