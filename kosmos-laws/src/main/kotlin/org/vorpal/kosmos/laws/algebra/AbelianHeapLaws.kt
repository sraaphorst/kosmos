package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianHeap
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.TernaryCommutativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [AbelianHeap] Laws:
 * - [HeapLaws]
 * - [TernaryCommutativityLaw]
 */
class AbelianHeapLaws<H : Any>(
    private val heap: AbelianHeap<H>,
    private val arb: Arb<H>,
    private val eq: Eq<H> = Eq.default(),
    private val pr: Printable<H> = Printable.default()
): LawSuite {
    private val dot = Symbols.DOT
    override val name = suiteName("Heap", "[$dot, $dot, $dot]")

    override fun laws(): List<TestingLaw> =
        HeapLaws(heap, arb, eq, pr).laws() +
        listOf(TernaryCommutativityLaw(heap.op, arb, eq, pr))
}
