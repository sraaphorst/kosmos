package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** StrongConnexity: R(a,b) ∨ R(b,a)
 *
 * Note that we allow a = b so reflexivity is implied. */
class StrongConnexity<A>(
    private val rel: Relation<A>,
    private val pairArb: Arb<Pair<A, A>>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "R",
    private val notSymbol: String = "¬$symbol"
) : TestingLaw {

    constructor(
        rel: Relation<A>,
        arb: Arb<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "R",
        notSymbol: String = "¬$symbol"
    ) : this(rel, Arb.pair(arb, arb), pr, symbol, notSymbol)

    override val name = "strong connexity ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            withClue(failureMessage(a, b)) {
                check(rel.rel(a, b) || rel.rel(b, a))
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        "Strong connexity failed: $sa $notSymbol $sb and $sb $notSymbol $sa"
    }
}
