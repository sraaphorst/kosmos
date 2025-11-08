package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** Transitivity: (a R b ∧ b R c) ⇒ a R c */
class TransitivityLaw<A: Any>(
    private val rel: Relation<A>,
    private val tripleArb: Arb<Triple<A, A, A>>,
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
    ) : this(rel, Arb.triple(arb, arb, arb), pr, symbol, notSymbol)

    override val name = "transitivity ($symbol)"

    override suspend fun test() {
        checkAll(tripleArb) { (a, b, c) ->
            if (rel(a, b) && rel(b, c)) {
                withClue(failureMessage(a, b, c)) {
                    check(rel(a, c))
                }
            }
        }
    }

    private fun failureMessage(a: A, b: A, c: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        "Transitivity failed: $sa $symbol $sb and $sb $symbol $sc, but $sa $notSymbol $sc"
    }
}
