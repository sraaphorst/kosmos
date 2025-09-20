package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** Symmetry: a R b ⇒ b R a */
class SymmetryLaw<A>(
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

    override val name = "symmetry ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val ab = rel.rel(a, b)
            val ba = rel.rel(b, a)
            withClue(failureMessage(a, b, ab, ba)) {
                check(ab == ba)
            }
        }
    }

    private fun failureMessage(
        a: A, b: A,
        ab: Boolean, ba: Boolean
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        if (ab && !ba)
            "Symmetry failed: $sa $symbol $sb but $sb $notSymbol $sa"
        else
            "Symmetry failed: $sa $notSymbol $sb but $sb $symbol $sa"
    }
}
