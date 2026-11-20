package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


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
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            if (rel(a, b)) {
                lawCheck(eq(a, b), failureMessage(a, b))
            }
        }
    }

    private fun failureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Coreflexivity failed: $sa $symbol $sb but $sa ≠ $sb"
    }
}
