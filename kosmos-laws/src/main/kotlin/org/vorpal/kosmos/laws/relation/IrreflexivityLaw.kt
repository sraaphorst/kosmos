package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Irreflexivity:
 *
 *    ¬(x R x)
 */
class IrreflexivityLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name = "irreflexivity ($symbol)"

    override suspend fun test() {
        checkAll(arb) { a ->
            val aa = rel(a, a)

            withClue(failureMessage(a, aa)) {
                check(!aa)
            }
        }
    }

    private fun failureMessage(a: A, aa: Boolean): () -> String = {
        val sa = pr(a)
        buildString {
            appendLine("Irreflexivity failed:")
            appendLine("a = $sa")
            appendLine("Observed: a $symbol a = $aa")
            appendLine("Expected: a $symbol a = false")
        }
    }
}
