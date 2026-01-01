package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Coreflexivity: x R y ⇒ x = y */
class CoreflexivityLaw<A: Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name = "coreflexivity ($symbol)"

    override suspend fun test() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            if (rel(a, b)) {
                withClue(failureMessage(a, b)) {
                    check(eq(a, b))
                }
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Coreflexivity failed: $sa $symbol $sb but $sa ≠ $sb"
    }
}
