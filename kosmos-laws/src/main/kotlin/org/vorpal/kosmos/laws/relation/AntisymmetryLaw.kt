package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** Antisymmetry: (a R b ∧ b R a) ⇒ a = b */
class AntisymmetryLaw<A: Any>(
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

    override val name = "antisymmetry ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val ab = rel(a, b)
            val ba = rel(b, a)

            // Only constrained when both directions hold.
            if (ab && ba) {
                withClue(failureMessage(a, b)) {
                    check(eq.eqv(a, b))
                }
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        "Antisymmetry failed: $sa $symbol $sb and $sb $symbol $sa, but $sa ≠ $sb"
    }
}