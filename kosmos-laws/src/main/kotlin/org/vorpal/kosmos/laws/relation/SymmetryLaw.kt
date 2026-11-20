package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/** Symmetry: a R b ⇒ b R a */
class SymmetryLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name = "symmetry ($symbol)"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            val ab = rel(a, b)

            if (ab) {
                val ba = rel(b, a)
                lawCheck(ba, failureMessage(a, b, ba))
            }
        }
    }

    private fun failureMessage(a: A, b: A, ba: Boolean): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)

            appendLine("Symmetry failed:")
            appendLine("a = $sa")
            appendLine("b = $sb")
            appendLine("Observed:")
            appendLine("  a $symbol b = true")
            appendLine("  b $symbol a = $ba")
            appendLine("Expected:")
            appendLine("  b $symbol a = true")
        }
    }
}