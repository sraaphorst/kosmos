package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/** Asymmetry: a R b ⇒ ¬(b R a) */
class AsymmetryLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name: String = "asymmetry ($symbol)"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            if (rel(a, b)) {
                lawCheck(!rel(b, a), failureMessage(a, b))
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Asymmetry failed: $sa $symbol $sb and $sb $symbol $sa"
    }
}