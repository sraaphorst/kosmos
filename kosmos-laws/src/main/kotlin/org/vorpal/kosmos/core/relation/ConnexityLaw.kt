package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Connexity (Totality): a≠b ⇒ R(a,b) ∨ R(b,a) */
class ConnexityLaw<A: Any>(
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

    override val name = "connexity ($symbol)"

    override suspend fun test() {
        checkAll(pairArb.distinct(eq)) { (a, b) ->
            withClue(failureMessage(a, b)) {
                check(rel(a, b) || rel(b, a))
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        "Connexity failed: $sa ≠ $sb, but $sa $notSymbol $sb and $sb $notSymbol $sa"
    }
}
