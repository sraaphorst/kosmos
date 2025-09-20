package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

/** Reflexivity: x R x */
class ReflexivityLaw<A>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "R",
    private val notSymbol: String = "¬$symbol"
) : TestingLaw {

    override val name = "reflexivity ($symbol)"

    override suspend fun test() {
        checkAll(arb) { a ->
            withClue(failureMessage(a)) {
                check(rel.rel(a, a))
            }
        }
    }

    private fun failureMessage(a: A): () -> String = {
        val sa = pr.render(a)
        "Reflexivity failed: $sa $notSymbol $sa"
    }
}
