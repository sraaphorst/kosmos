package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeSemigroup
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeSemigroup] laws:
 * - [MagmaLaws]
 *
 * A [NonAssociativeSemigroup] is just a magma by a different name.
 *
 * We will henceforth use this class in law extensions instead of [MagmaLaws].
 */
class NonAssociativeSemigroupLaws<A : Any>(
    private val semigroup: NonAssociativeSemigroup<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default(),
): LawSuite {

    override val name = suiteName("NonAssociativeSemigroup", semigroup.op.symbol)

    override fun laws(): List<TestingLaw> =
        MagmaLaws(semigroup, arb, pr).laws()
}