package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

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
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val aa = rel(a, a)

            lawCheck(!aa, failureMessage(a, aa))
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
