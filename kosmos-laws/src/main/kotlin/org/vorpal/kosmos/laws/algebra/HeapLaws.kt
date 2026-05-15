package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Heap
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.property.BiunitaryLaw
import org.vorpal.kosmos.laws.property.ParaAssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Heap] laws:
 * - [ParaAssociativityLaw]
 * - [BiunitaryLaw]
 */
class HeapLaws<H : Any>(
    private val heap: Heap<H>,
    private val arb: Arb<H>,
    private val eq: Eq<H> = Eq.default(),
    private val pr: Printable<H> = Printable.default()
): LawSuite {
    private val dot = Symbols.DOT
    override val name = suiteName("Heap", "[$dot, $dot, $dot]")

    override fun laws() = listOf(
        ParaAssociativityLaw(heap.op, arb, eq, pr),
        BiunitaryLaw(heap.op, arb, eq, pr)
    )
}
