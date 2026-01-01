package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Symmetry: a R b ⇒ b R a */
class SymmetryLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name = "symmetry ($symbol)"

    override suspend fun test() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            val ab = rel(a, b)

            if (ab) {
                val ba = rel(b, a)
                withClue(failureMessage(a, b, ba)) {
                    check(ba)
                }
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