package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** Exactly one of aRb, bRa, or a = b
 * Note that this expects a strict (i.e. irreflexive, transitive) order, e.g. <. */
class TrichotomyLaw<A: Any>(
    private val rel: Relation<A>,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "R",
    private val notSymbol: String = "¬$symbol"
) : TestingLaw {

    constructor(
        rel: Relation<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "R",
        notSymbol: String = "¬$symbol"
    ) : this(rel, Arb.pair(arb, arb), eq, pr, symbol, notSymbol)

    override val name = "trichotomy ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val ab = rel(a, b)
            val ba = rel(b, a)
            val aeb = eq.eqv(a, b)

            withClue(failureMessage(a, b, ab, ba, aeb)) {
                check((ab && !ba && !aeb) || (!ab && ba && !aeb) || (!ab && !ba && aeb))
            }
        }
    }

    private fun failureMessage(
        a: A, b: A, ab: Boolean, ba: Boolean, aeb: Boolean
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)

        buildString {
            appendLine("Trichotomy failed:")
            appendLine("$sa $symbol $sb: $ab")
            appendLine("$sb $symbol $sa: $ba")
            appendLine("$sa ${if (aeb) "=" else "≠"} $sb")
        }
    }
}
