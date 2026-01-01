package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** StrongConnexity: R(a,b) ∨ R(b,a)
 *
 * Note that we allow a = b so reflexivity is implied. */
class StrongConnexityLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name = "strong connexity ($symbol)"

    override suspend fun test() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            val ab = rel(a, b)
            val ba = rel(b, a)

            withClue(failureMessage(a, b, ab, ba)) {
                check(ab || ba)
            }
        }
    }

    private fun failureMessage(a: A, b: A, ab: Boolean, ba: Boolean): () -> String = {
        val sa = pr(a)
        val sb = pr(b)

        buildString {
            appendLine("Strong connexity failed:")
            appendLine("a = $sa")
            appendLine("b = $sb")
            appendLine("Observed:")
            appendLine("  a $symbol b = $ab")
            appendLine("  b $symbol a = $ba")
            appendLine("Expected:")
            appendLine("  (a $symbol b) OR (b $symbol a) = true")
        }
    }
}
